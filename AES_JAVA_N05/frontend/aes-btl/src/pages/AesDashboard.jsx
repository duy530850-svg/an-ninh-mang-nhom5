import React, { useState, useEffect } from "react";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import axios from "axios";
import {
  ConfigProvider,
  theme,
  Radio,
  Select,
  Input,
  Button,
  Tabs,
  Tooltip,
  message,
  notification,
  Segmented,
  Upload,
  Progress,
} from "antd";
import {
  KeyOutlined,
  LockOutlined,
  UnlockOutlined,
  CopyOutlined,
  ReloadOutlined,
  ThunderboltOutlined,
  InboxOutlined,
  FileOutlined,
  InfoCircleOutlined,
  DownloadOutlined,
} from "@ant-design/icons";

// Import Schema
import { configSchema, encryptSchema, decryptSchema } from "@/utils/validation";

// Import Header
import Header from "@/components/Header";

const { TextArea } = Input;
const { Dragger } = Upload;
const BACKEND_URL = "http://localhost:8080/api/aes";

export default function AesDashboard() {
  const [apiConnected, setApiConnected] = useState(false);
  const [activeTab, setActiveTab] = useState("encrypt");
  const [encryptStatusText, setEncryptStatusText] = useState("");
  const [decryptStatusText, setDecryptStatusText] = useState("");

  const [encryptInputType, setEncryptInputType] = useState("TEXT");
  const [decryptInputType, setDecryptInputType] = useState("TEXT");

  const [encryptResult, setEncryptResult] = useState("");
  const [decryptResult, setDecryptResult] = useState("");

  const [encryptFile, setEncryptFile] = useState(null);
  const [decryptFile, setDecryptFile] = useState(null);
  const [encryptProgress, setEncryptProgress] = useState(0);
  const [decryptProgress, setDecryptProgress] = useState(0);

  const [encryptLoading, setEncryptLoading] = useState(false);
  const [decryptLoading, setDecryptLoading] = useState(false);

  const configForm = useForm({
    resolver: zodResolver(configSchema),
    defaultValues: {
      keySize: 128,
      mode: "CBC",
      keyFormat: "HEX",
      key: "",
      ivFormat: "HEX",
      iv: "",
    },
  });
  const encryptForm = useForm({
    resolver: zodResolver(encryptSchema),
    defaultValues: { plaintext: "", outputFormat: "BASE64" },
  });
  const decryptForm = useForm({
    resolver: zodResolver(decryptSchema),
    defaultValues: { ciphertext: "", inputFormat: "BASE64" },
  });

  const currentMode = configForm.watch("mode");

  useEffect(() => {
    fetch(`${BACKEND_URL}/generate-params?keySize=128&needIv=false`)
      .then((res) => {
        if (res.ok) setApiConnected(true);
      })
      .catch(() => setApiConnected(false));
  }, []);

  const handleGenerateParams = async () => {
    const keySizeVal = configForm.getValues("keySize");
    const modeVal = configForm.getValues("mode");
    const currentFormat = configForm.getValues("keyFormat");
    const needIv = modeVal === "CBC";

    try {
      const response = await fetch(
        `${BACKEND_URL}/generate-params?keySize=${keySizeVal}&needIv=${needIv}&format=${currentFormat}`,
      );

      if (response.ok) {
        const data = await response.json();

        configForm.setValue("key", data.key, { shouldValidate: true });

        if (needIv) {
          configForm.setValue("ivFormat", currentFormat);
          configForm.setValue("iv", data.iv, { shouldValidate: true });
        }

        message.success(`Đã tự động sinh cấu hình Khóa dạng ${currentFormat}!`);
      }
    } catch (error) {
      message.error("Lỗi kết nối máy chủ sinh tham số.");
    }
  };

  const handleTextEncrypt = async () => {
    const isConfigValid = await configForm.trigger();
    const isEncryptValid = await encryptForm.trigger();
    if (!isConfigValid || !isEncryptValid) return;

    setEncryptLoading(true);
    const payload = { ...configForm.getValues(), ...encryptForm.getValues() };
    try {
      const { data } = await axios.post(`${BACKEND_URL}/encrypt`, payload);
      if (data.success) {
        setEncryptResult(data.result);
        message.success("Mã hóa văn bản thành công!");
      } else {
        notification.error({
          message: "Lỗi mã hóa",
          description: data.errorMessage,
        });
      }
    } catch (error) {
      message.error("Lỗi kết nối mạng.");
    } finally {
      setEncryptLoading(false);
    }
  };

  const handleTextDecrypt = async () => {
    const isConfigValid = await configForm.trigger();
    const isDecryptValid = await decryptForm.trigger();
    if (!isConfigValid || !isDecryptValid) return;

    setDecryptLoading(true);
    const payload = { ...configForm.getValues(), ...decryptForm.getValues() };
    try {
      const { data } = await axios.post(`${BACKEND_URL}/decrypt`, payload);
      if (data.success) {
        setDecryptResult(data.result);
        message.success("Giải mã văn bản thành công!");
      } else {
        notification.error({
          message: "Lỗi giải mã",
          description: data.errorMessage,
        });
      }
    } catch (error) {
      message.error("Lỗi kết nối mạng.");
    } finally {
      setDecryptLoading(false);
    }
  };

  const processFile = async (actionType) => {
    const isConfigValid = await configForm.trigger();
    if (!isConfigValid) return;

    const file = actionType === "ENCRYPT" ? encryptFile : decryptFile;
    if (!file) return message.error("Vui lòng chọn một tập tin để xử lý!");

    const configData = configForm.getValues();
    const formData = new FormData();
    formData.append("file", file);
    formData.append("key", configData.key);
    formData.append("keySize", configData.keySize);
    formData.append("mode", configData.mode);
    if (configData.mode === "CBC") formData.append("iv", configData.iv);

    const setProgress =
      actionType === "ENCRYPT" ? setEncryptProgress : setDecryptProgress;
    const setStatusText =
      actionType === "ENCRYPT" ? setEncryptStatusText : setDecryptStatusText;
    const setLoading =
      actionType === "ENCRYPT" ? setEncryptLoading : setDecryptLoading;
    const endpoint =
      actionType === "ENCRYPT" ? "/file/encrypt" : "/file/decrypt";

    setLoading(true);
    setProgress(0);
    setStatusText("Đang chuẩn bị gửi tập tin...");

    try {
      const response = await axios.post(`${BACKEND_URL}${endpoint}`, formData, {
        responseType: "blob",
        headers: { "Content-Type": "multipart/form-data" },

        // 1. Theo dõi tiến trình tải file LÊN server
        onUploadProgress: (e) => {
          const percent = Math.round((e.loaded * 100) / e.total);
          if (percent < 100) {
            setProgress(percent);
            setStatusText(`Đang tải tập tin lên máy chủ: ${percent}%`);
          } else {
            setProgress(100);
            setStatusText("Máy chủ đang thực thi thuật toán AES..."); // Khắc phục khoảng lặng xử lý
          }
        },

        // 2. Theo dõi tiến trình tải file kết quả VỀ trình duyệt
        onDownloadProgress: (e) => {
          if (e.total) {
            const percent = Math.round((e.loaded * 100) / e.total);
            setProgress(percent);
            setStatusText(`Đang tải dữ liệu kết quả về: ${percent}%`);
          } else {
            setStatusText("Đang chuẩn bị lưu tập tin xuống máy...");
          }
        },
      });

      setStatusText("Đang ghi file ra đĩa...");
      const contentDisposition = response.headers["content-disposition"];
      let filename =
        actionType === "ENCRYPT"
          ? `${file.name}.enc`
          : `decrypted_${file.name.replace(".enc", "")}`;
      if (contentDisposition) {
        const match = contentDisposition.match(
          /filename\*?=['"]?(?:UTF-\d['"]*)?([^;\r\n"']*)['"]?/i,
        );
        if (match && match[1]) filename = decodeURIComponent(match[1]);
      }

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      message.success(
        `${actionType === "ENCRYPT" ? "Mã hóa" : "Giải mã"} tập tin thành công!`,
      );
      setStatusText("Hoàn tất!");
    } catch (error) {
      setStatusText("Thất bại!");
      if (error.response && error.response.data instanceof Blob) {
        const textError = await error.response.data.text();
        notification.error({
          message: `Lỗi xử lý Tập Tin`,
          description: textError,
        });
      } else {
        message.error("Lỗi kết nối hoặc kích thước tập tin vượt quá giới hạn.");
      }
    } finally {
      setLoading(false);
      setTimeout(() => {
        setProgress(0);
        setStatusText("");
      }, 3000); // Tự động ẩn thanh trạng thái sau 3s
    }
  };

  const draggerProps = (setFileState) => ({
    name: "file",
    multiple: false,
    showUploadList: true,
    maxCount: 1,
    beforeUpload: (file) => {
      if (file.size / 1024 / 1024 > 50) {
        message.error("Tập tin phải nhỏ hơn 50MB!");
        return Upload.LIST_IGNORE;
      }
      setFileState(file);
      return false;
    },
    onRemove: () => setFileState(null),
  });

  const copyToClipboard = (text) => {
    if (!text) return message.warning("Không có nội dung!");
    navigator.clipboard.writeText(text);
    message.success("Đã sao chép vào clipboard!");
  };

  const downloadTextAsFile = (text, defaultFilename) => {
    if (!text) return message.warning("Chưa có kết quả để lưu!");

    const blob = new Blob([text], { type: "text/plain;charset=utf-8" });
    const url = window.URL.createObjectURL(blob);

    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", defaultFilename);
    document.body.appendChild(link);
    link.click();

    link.remove();
    window.URL.revokeObjectURL(url);

    message.success(`Đã lưu thành file: ${defaultFilename}`);
  };

  // --- CỘT TRÁI (CẤU HÌNH) ---
  const renderConfigPanel = () => {
    const {
      control,
      formState: { errors },
    } = configForm;
    return (
      <div className="flex flex-col gap-6">
        <div>
          <label className="block text-sm font-semibold text-slate-300 mb-2 font-mono">
            Độ dài khóa (bit)
          </label>
          <Controller
            name="keySize"
            control={control}
            render={({ field }) => (
              <Radio.Group
                {...field}
                size="large"
                className="w-full grid grid-cols-3 text-center"
              >
                <Radio.Button value={128}>128-bit</Radio.Button>
                <Radio.Button value={192}>192-bit</Radio.Button>
                <Radio.Button value={256}>256-bit</Radio.Button>
              </Radio.Group>
            )}
          />
        </div>

        <div>
          <label className="block text-sm font-semibold text-slate-300 mb-2 font-mono">
            Chế độ hoạt động
          </label>
          <Controller
            name="mode"
            control={control}
            render={({ field }) => (
              <Select
                {...field}
                size="large"
                className="w-full text-base"
                options={[
                  { value: "ECB", label: "ECB (Tốc độ cao)" },
                  { value: "CBC", label: "CBC (Bảo mật cao hơn ECB)" },
                ]}
              />
            )}
          />
        </div>

        <div>
          <div className="flex justify-between items-center mb-2">
            <label className="block text-sm font-semibold text-slate-300 font-mono">
              Khóa bảo mật
            </label>
            <Controller
              name="keyFormat"
              control={control}
              render={({ field }) => (
                <Select
                  {...field}
                  size="large"
                  className="w-28 text-sm"
                  options={[
                    { value: "PLAIN_TEXT", label: "Văn bản rõ" },
                    { value: "HEX", label: "HEX" },
                    { value: "BASE64", label: "Base64" },
                  ]}
                />
              )}
            />
          </div>
          <Controller
            name="key"
            control={control}
            render={({ field }) => (
              <Input
                {...field}
                size="large"
                placeholder="Nhập khóa..."
                className="font-mono text-lg py-2"
                status={errors.key ? "error" : ""}
              />
            )}
          />
          {errors.key && (
            <p className="text-red-500 text-sm mt-1">{errors.key.message}</p>
          )}
        </div>

        {currentMode === "CBC" && (
          <div className="transition-all duration-300">
            <div className="flex justify-between items-center mb-2">
              <label className="block text-sm font-semibold text-slate-300 font-mono">
                Véc-tơ IV
              </label>
              <Controller
                name="ivFormat"
                control={control}
                render={({ field }) => (
                  <Select
                    {...field}
                    size="large"
                    className="w-28 text-sm"
                    options={[
                      { value: "PLAIN_TEXT", label: "Văn bản rõ" },
                      { value: "HEX", label: "HEX" },
                      { value: "BASE64", label: "Base64" },
                    ]}
                  />
                )}
              />
            </div>
            <Controller
              name="iv"
              control={control}
              render={({ field }) => (
                <Input
                  {...field}
                  size="large"
                  placeholder="Nhập véc-tơ IV..."
                  className="font-mono text-lg py-2"
                  status={errors.iv ? "error" : ""}
                />
              )}
            />
            {errors.iv && (
              <p className="text-red-500 text-sm mt-1">{errors.iv.message}</p>
            )}
          </div>
        )}

        <Button
          type="dashed"
          size="large"
          icon={<ReloadOutlined />}
          onClick={handleGenerateParams}
          className="mt-2 text-emerald-400 h-12 text-base font-semibold"
        >
          Sinh ngẫu nhiên
        </Button>
      </div>
    );
  };

  // --- CẤU TRÚC TABS ---
  const tabItems = [
    {
      key: "encrypt",
      label: (
        <span className="font-bold text-base px-3 py-1">
          <LockOutlined /> Mã hóa (encrypt)
        </span>
      ),
      children: (
        <div className="mt-2">
          <div className="flex justify-center mb-2">
            <Segmented
              options={[
                {
                  label: (
                    <span className="px-4 py-1 text-base">📝 Nhập văn bản</span>
                  ),
                  value: "TEXT",
                },
                {
                  label: (
                    <span className="px-4 py-1 text-base">📁 Tải tập tin</span>
                  ),
                  value: "FILE",
                },
              ]}
              value={encryptInputType}
              onChange={setEncryptInputType}
              size="large"
            />
          </div>

          {encryptInputType === "TEXT" ? (
            <form className="flex flex-col gap-6">
              <div>
                <label className="block text-sm font-semibold text-slate-400 mb-2 font-mono">
                  Văn bản rõ (plaintext)
                </label>
                <Controller
                  name="plaintext"
                  control={encryptForm.control}
                  render={({ field }) => (
                    <TextArea
                      {...field}
                      rows={4}
                      placeholder="Nhập văn bản..."
                      className="text-lg p-4 bg-slate-950 border-slate-700"
                      status={
                        encryptForm.formState.errors.plaintext ? "error" : ""
                      }
                    />
                  )}
                />
                {encryptForm.formState.errors.plaintext && (
                  <p className="text-red-500 text-sm mt-1">
                    {encryptForm.formState.errors.plaintext.message}
                  </p>
                )}
              </div>

              <div className="flex justify-between items-center">
                <div className="flex items-center gap-3">
                  <span className="text-sm font-semibold text-slate-400 font-mono">
                    Định dạng đầu ra:
                  </span>
                  <Controller
                    name="outputFormat"
                    control={encryptForm.control}
                    render={({ field }) => (
                      <Select
                        {...field}
                        size="large"
                        className="w-32"
                        options={[
                          { value: "HEX", label: "HEX" },
                          { value: "BASE64", label: "Base64" },
                        ]}
                      />
                    )}
                  />
                </div>
                <Button
                  type="primary"
                  onClick={handleTextEncrypt}
                  loading={encryptLoading}
                  size="large"
                  className="bg-emerald-600 hover:bg-emerald-500 font-bold h-12 px-8 text-base"
                >
                  Thực hiện mã hóa text
                </Button>
              </div>

              <div className="border-t border-slate-800 pt-5 mt-2">
                <div className="flex justify-between items-center mb-2">
                  <label className="block text-sm font-semibold text-emerald-400 font-mono">
                    Bản mã trả về
                  </label>
                  <div className="flex gap-2">
                    {" "}
                    {/* Nhóm 2 nút vào thẻ div flex */}
                    <Tooltip title="Lưu thành file .txt">
                      <Button
                        size="large"
                        icon={<DownloadOutlined />}
                        onClick={() =>
                          downloadTextAsFile(
                            encryptResult,
                            "ciphertext_result.txt",
                          )
                        }
                        disabled={!encryptResult} // Khóa nút nếu chưa có kết quả
                        className="bg-slate-950 border-slate-700 hover:border-emerald-500 text-emerald-400"
                      />
                    </Tooltip>
                    <Tooltip title="Copy kết quả">
                      <Button
                        size="large"
                        icon={<CopyOutlined />}
                        onClick={() => copyToClipboard(encryptResult)}
                        disabled={!encryptResult}
                        className="bg-slate-950 border-slate-700 hover:border-slate-500"
                      />
                    </Tooltip>
                  </div>
                </div>
                <TextArea
                  rows={4}
                  readOnly
                  value={encryptResult}
                  className="text-emerald-400 p-4 font-mono text-base bg-slate-950 border-slate-700"
                  placeholder="Chờ kết quả..."
                />
              </div>
            </form>
          ) : (
            <div className="flex flex-col gap-5">
              <Dragger
                {...draggerProps(setEncryptFile)}
                className="bg-slate-950 border-slate-700 hover:border-emerald-500 p-10"
              >
                <p className="ant-upload-drag-icon">
                  <InboxOutlined className="text-emerald-500 text-5xl" />
                </p>
                <p className="text-xl font-bold text-slate-300 mt-4">
                  Kéo thả tập tin vào đây để mã hóa
                </p>
                <p className="text-slate-500 mt-2 text-base">
                  Hỗ trợ mọi định dạng (PDF, JPG, MP4, ZIP...). Tối đa 50MB.
                </p>
              </Dragger>

              <Button
                type="primary"
                onClick={() => processFile("ENCRYPT")}
                loading={encryptLoading}
                disabled={!encryptFile}
                size="large"
                className="bg-emerald-600 hover:bg-emerald-500 font-bold h-14 text-lg"
              >
                Mã hóa và tải file xuống
              </Button>

              {encryptProgress > 0 && (
                <div className="mt-3">
                  <div className="text-sm font-mono text-emerald-400 mb-1 font-semibold">
                    {encryptStatusText}
                  </div>
                  <Progress
                    percent={encryptProgress}
                    strokeColor="#10b981"
                    showInfo={false}
                  />
                </div>
              )}
            </div>
          )}
        </div>
      ),
    },
    {
      key: "decrypt",
      label: (
        <span className="font-bold text-base px-3 py-1">
          <UnlockOutlined /> Giải mã (decrypt)
        </span>
      ),
      children: (
        <div className="mt-2">
          <div className="flex justify-center mb-2">
            <Segmented
              options={[
                {
                  label: (
                    <span className="px-4 py-1 text-base">
                      📝 Nhập bản mã (text)
                    </span>
                  ),
                  value: "TEXT",
                },
                {
                  label: (
                    <span className="px-4 py-1 text-base">
                      📁 Tải file đã mã hóa
                    </span>
                  ),
                  value: "FILE",
                },
              ]}
              value={decryptInputType}
              onChange={setDecryptInputType}
              size="large"
            />
          </div>

          {decryptInputType === "TEXT" ? (
            <form className="flex flex-col gap-5">
              <div>
                <label className="block text-sm font-semibold text-slate-400 mb-2 font-mono">
                  Bản mã đầu vào (ciphertext)
                </label>
                <Controller
                  name="ciphertext"
                  control={decryptForm.control}
                  render={({ field }) => (
                    <TextArea
                      {...field}
                      rows={4}
                      placeholder="Nhập bản mã (Hex/Base64)..."
                      className="text-lg font-mono p-4 bg-slate-950 border-slate-700"
                      status={
                        decryptForm.formState.errors.ciphertext ? "error" : ""
                      }
                    />
                  )}
                />
                {decryptForm.formState.errors.ciphertext && (
                  <p className="text-red-500 text-sm mt-1">
                    {decryptForm.formState.errors.ciphertext.message}
                  </p>
                )}
              </div>

              <div className="flex justify-between items-center">
                <div className="flex items-center gap-3">
                  <span className="text-sm font-semibold text-slate-400 font-mono">
                    Định dạng bản mã:
                  </span>
                  <Controller
                    name="inputFormat"
                    control={decryptForm.control}
                    render={({ field }) => (
                      <Select
                        {...field}
                        size="large"
                        className="w-32"
                        options={[
                          { value: "HEX", label: "HEX" },
                          { value: "BASE64", label: "Base64" },
                        ]}
                      />
                    )}
                  />
                </div>
                <Button
                  type="primary"
                  onClick={handleTextDecrypt}
                  loading={decryptLoading}
                  size="large"
                  className="bg-indigo-600 hover:bg-indigo-500 font-bold h-12 px-8 text-base"
                >
                  Thực hiện giải mã text
                </Button>
              </div>
              <div className="border-t border-slate-800 pt-5 mt-2">
                <div className="flex justify-between items-center mb-2">
                  <label className="block text-sm font-semibold text-indigo-400 font-mono">
                    Văn bản rõ khôi phục
                  </label>
                  <div className="flex gap-2">
                    <Tooltip title="Lưu thành file .txt">
                      <Button
                        size="large"
                        icon={<DownloadOutlined />}
                        onClick={() =>
                          downloadTextAsFile(
                            decryptResult,
                            "plaintext_result.txt",
                          )
                        }
                        disabled={!decryptResult}
                        className="bg-slate-950 border-slate-700 hover:border-indigo-500 text-indigo-400"
                      />
                    </Tooltip>
                    <Tooltip title="Copy kết quả">
                      <Button
                        size="large"
                        icon={<CopyOutlined />}
                        onClick={() => copyToClipboard(decryptResult)}
                        disabled={!decryptResult}
                        className="bg-slate-950 border-slate-700 hover:border-slate-500"
                      />
                    </Tooltip>
                  </div>
                </div>
                <TextArea
                  rows={4}
                  readOnly
                  value={decryptResult}
                  className="text-indigo-300 p-4 text-lg bg-slate-950 border-slate-700"
                  placeholder="Chờ kết quả..."
                />
              </div>
            </form>
          ) : (
            <div className="flex flex-col gap-6">
              <Dragger
                {...draggerProps(setDecryptFile)}
                className="bg-slate-950 border-slate-700 hover:border-indigo-500 p-10"
              >
                <p className="ant-upload-drag-icon">
                  <FileOutlined className="text-indigo-500 text-5xl" />
                </p>
                <p className="text-xl font-bold text-slate-300 mt-4">
                  Kéo thả file đã mã hóa (.enc) vào đây
                </p>
                <p className="text-slate-500 mt-2 text-base">
                  Vui lòng đảm bảo cấu hình khóa & IV ở cột trái chính xác trước
                  khi giải mã.
                </p>
              </Dragger>

              <Button
                type="primary"
                onClick={() => processFile("DECRYPT")}
                loading={decryptLoading}
                disabled={!decryptFile}
                size="large"
                className="bg-indigo-600 hover:bg-indigo-500 font-bold h-14 text-lg"
              >
                Giải mã và khôi phục file gốc
              </Button>

              {decryptProgress > 0 && (
                <div className="mt-3">
                  <div className="text-sm font-mono text-indigo-400 mb-1 font-semibold">
                    {decryptStatusText}
                  </div>
                  <Progress
                    percent={decryptProgress}
                    strokeColor="#4f46e5"
                    showInfo={false}
                  />
                </div>
              )}
            </div>
          )}
        </div>
      ),
    },
  ];

  return (
    <ConfigProvider theme={{ algorithm: theme.darkAlgorithm }}>
      <div className="min-h-screen bg-slate-950 text-slate-100 p-6 md:p-8">
        <Header apiConnected={apiConnected} />

        <main className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-12 gap-8">
          <div className="md:col-span-4 bg-slate-900 border border-slate-800 rounded-xl p-8 h-fit shadow-2xl">
            <h2 className="text-lg font-bold border-b border-slate-800 pb-4 mb-6 flex items-center gap-2 text-slate-200 font-mono tracking-wide">
              <KeyOutlined className="text-emerald-400 text-xl" /> THÔNG SỐ CẤU
              HÌNH
            </h2>
            {renderConfigPanel()}
          </div>

          <div className="md:col-span-8 bg-slate-900 border border-slate-800 rounded-xl p-7 shadow-2xl">
            <Tabs
              activeKey={activeTab}
              onChange={setActiveTab}
              items={tabItems}
            />
          </div>
        </main>
      </div>
    </ConfigProvider>
  );
}
