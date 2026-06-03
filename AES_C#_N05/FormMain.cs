using System;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using System.Windows.Forms;

namespace AES_C__N05
{
    public partial class FormMain : Form
    {
        public FormMain()
        {
            InitializeComponent();
        }

        private void FormMain_Load(object sender, EventArgs e)
        {
            cboAESVersion.Items.Add("AES-128");
            cboAESVersion.Items.Add("AES-192");
            cboAESVersion.Items.Add("AES-256");

            cboAESVersion.SelectedIndex = 2;

            lblStatus.Text = "Trạng thái: Sẵn sàng";
        }

        private void rtbKey_TextChanged(object sender, EventArgs e)
        {

        }

        private int GetSelectedKeySize()
        {
            switch (cboAESVersion.Text)
            {
                case "AES-128":
                    return 128;

                case "AES-192":
                    return 192;

                default:
                    return 256;
            }
        }

        private void btnGenerateKey_Click(object sender, EventArgs e)
        {
            try
            {
                int keySize = GetSelectedKeySize();

                using (Aes aes = Aes.Create())
                {
                    aes.KeySize = keySize;
                    aes.GenerateKey();

                    rtbKey.Text = Convert.ToBase64String(aes.Key);
                }

                lblStatus.Text = "Trạng thái: Đã tạo khóa AES";
            }
            catch
            {
                MessageBox.Show(
                    "Không thể tạo khóa AES.",
                    "Lỗi",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
            }
        }

        private void btnEncrypt_Click(object sender, EventArgs e)
        {
            if (string.IsNullOrWhiteSpace(rtbInput.Text))
            {
                MessageBox.Show(
                    "Vui lòng nhập dữ liệu cần mã hóa.",
                    "Cảnh báo",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Warning);
                return;
            }

            if (string.IsNullOrWhiteSpace(rtbKey.Text))
            {
                MessageBox.Show(
                    "Vui lòng tạo hoặc nhập khóa AES.",
                    "Cảnh báo",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Warning);
                return;
            }

            try
            {
                string plainText = rtbInput.Text.Trim();
                string keyString = rtbKey.Text.Trim();

                byte[] key = Convert.FromBase64String(keyString);

                using (Aes aes = Aes.Create())
                {
                    aes.Key = key;
                    aes.GenerateIV();

                    byte[] iv = aes.IV;

                    ICryptoTransform encryptor =
                        aes.CreateEncryptor(aes.Key, aes.IV);

                    byte[] plainBytes =
                        Encoding.UTF8.GetBytes(plainText);

                    byte[] encryptedBytes =
                        encryptor.TransformFinalBlock(
                            plainBytes,
                            0,
                            plainBytes.Length);

                    byte[] result =
                        new byte[iv.Length + encryptedBytes.Length];

                    Buffer.BlockCopy(iv, 0, result, 0, iv.Length);

                    Buffer.BlockCopy(
                        encryptedBytes,
                        0,
                        result,
                        iv.Length,
                        encryptedBytes.Length);

                    rtbOutput.Text =
                        Convert.ToBase64String(result);
                }

                lblStatus.Text = "Trạng thái: Mã hóa thành công";

            }
            catch (FormatException)
            {
                MessageBox.Show(
                    "Khóa AES không đúng định dạng.",
                    "Lỗi",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
            }
            catch
            {
                MessageBox.Show(
                    "Có lỗi xảy ra khi mã hóa dữ liệu.",
                    "Lỗi",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
            }
        }

        private void btnDecrypt_Click(object sender, EventArgs e)
        {
            if (string.IsNullOrWhiteSpace(rtbInput.Text))
            {
                MessageBox.Show(
                    "Vui lòng nhập dữ liệu cần giải mã.",
                    "Cảnh báo",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Warning);
                return;
            }

            if (string.IsNullOrWhiteSpace(rtbKey.Text))
            {
                MessageBox.Show(
                    "Vui lòng nhập khóa AES.",
                    "Cảnh báo",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Warning);
                return;
            }

            try
            {
                byte[] fullCipher =
                    Convert.FromBase64String(rtbInput.Text.Trim());

                byte[] key =
                    Convert.FromBase64String(rtbKey.Text.Trim());

                using (Aes aes = Aes.Create())
                {
                    aes.Key = key;

                    byte[] iv = new byte[16];

                    Array.Copy(fullCipher, 0, iv, 0, 16);

                    aes.IV = iv;

                    byte[] cipher =
                        new byte[fullCipher.Length - 16];

                    Array.Copy(
                        fullCipher,
                        16,
                        cipher,
                        0,
                        cipher.Length);

                    ICryptoTransform decryptor =
                        aes.CreateDecryptor(aes.Key, aes.IV);

                    byte[] decryptedBytes =
                        decryptor.TransformFinalBlock(
                            cipher,
                            0,
                            cipher.Length);

                    rtbOutput.Text =
                        Encoding.UTF8.GetString(decryptedBytes);
                }

                lblStatus.Text = "Trạng thái: Giải mã thành công";

     
            }
            catch (FormatException)
            {
                MessageBox.Show(
                    "Dữ liệu mã hóa không đúng định dạng Base64.",
                    "Lỗi",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
            }
            catch (CryptographicException)
            {
                MessageBox.Show(
                    "Khóa AES không chính xác hoặc dữ liệu đã bị thay đổi.",
                    "Lỗi",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
            }
            catch
            {
                MessageBox.Show(
                    "Không thể giải mã dữ liệu.",
                    "Lỗi",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
            }
        }

        private void btnOpenFile_Click(object sender, EventArgs e)
        {
            OpenFileDialog ofd = new OpenFileDialog();

            ofd.Filter = "Text Files (*.txt)|*.txt|All Files (*.*)|*.*";

            if (ofd.ShowDialog() == DialogResult.OK)
            {
                try
                {
                    rtbInput.Text = File.ReadAllText(ofd.FileName);

                    lblStatus.Text = "Trạng thái: Đã mở file";
                }
                catch
                {
                    MessageBox.Show(
                        "Không thể đọc file.",
                        "Lỗi",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Error);
                }
            }
        }

        private void btnSave_Click(object sender, EventArgs e)
        {
            if (string.IsNullOrWhiteSpace(rtbOutput.Text))
            {
                MessageBox.Show(
                    "Không có dữ liệu để lưu.",
                    "Cảnh báo",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Warning);
                return;
            }

            SaveFileDialog sfd = new SaveFileDialog();

            sfd.Filter = "Text Files (*.txt)|*.txt";

            if (sfd.ShowDialog() == DialogResult.OK)
            {
                try
                {
                    File.WriteAllText(
                        sfd.FileName,
                        rtbOutput.Text);

                    lblStatus.Text = "Trạng thái: Đã lưu file";
                }
                catch
                {
                    MessageBox.Show(
                        "Không thể lưu file.",
                        "Lỗi",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Error);
                }
            }
        }

        private void btnClear_Click(object sender, EventArgs e)
        {
            DialogResult result = MessageBox.Show(
                "Bạn có chắc muốn xóa toàn bộ dữ liệu?",
                "Xác nhận",
                MessageBoxButtons.YesNo,
                MessageBoxIcon.Question);

            if (result == DialogResult.Yes)
            {
                rtbInput.Clear();
                rtbKey.Clear();
                rtbOutput.Clear();

                lblStatus.Text = "Trạng thái: Đã xóa dữ liệu";
            }
        }
    }
}