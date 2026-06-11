import React from "react";
import { ThunderboltOutlined } from "@ant-design/icons";

export default function Header({ apiConnected }) {
  return (
    <header className="max-w-7xl mx-auto mb-10 border-b border-slate-800 pb-5 flex justify-between items-center">
      <div className="flex items-center gap-3">
        <ThunderboltOutlined className="text-emerald-400 text-2xl animate-pulse" />
        <h1 className="text-2xl font-extrabold text-emerald-400 font-mono tracking-wider">
          AES.WEB_NHOM5
        </h1>
      </div>
      <div className="flex items-center gap-2">
        <span
          className={`w-3 h-3 rounded-full ${apiConnected ? "bg-emerald-500" : "bg-red-500"} animate-pulse`}
        ></span>
        <span className="text-sm font-semibold text-slate-400 font-mono tracking-wide">
          {apiConnected ? "Backend đã kết nối" : "Backend chưa kết nối"}
        </span>
      </div>
    </header>
  );
}
