import React, { useState } from 'react';
import { assistantApi } from '../services/api';
import { ExplainImageResponse } from '../types';
import {
  UploadCloud,
  Image as ImageIcon,
  ShieldAlert,
  AlertTriangle,
  CheckCircle2,
  HelpCircle,
  Loader2,
  X,
  Sparkles,
  ArrowRight,
  EyeOff,
} from 'lucide-react';

export const ScreenshotExplainerView: React.FC = () => {
  const [imageBase64, setImageBase64] = useState<string | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [userNote, setUserNote] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [result, setResult] = useState<ExplainImageResponse | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // File handling
  const handleFileChange = (file: File) => {
    setErrorMessage(null);
    setResult(null);

    // Validate type
    if (!file.type.startsWith('image/')) {
      setErrorMessage('Please upload a valid image file (PNG, JPG, JPEG, or WEBP).');
      return;
    }

    // Validate size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      setErrorMessage('Image size is too large. Please select an image under 5MB.');
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const base64 = reader.result as string;
      setImageBase64(base64);
      setImagePreview(base64);
    };
    reader.onerror = () => {
      setErrorMessage('Failed to read image file. Please try another image.');
    };
    reader.readAsDataURL(file);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileChange(e.dataTransfer.files[0]);
    }
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    if (e.clipboardData.files && e.clipboardData.files[0]) {
      handleFileChange(e.clipboardData.files[0]);
    }
  };

  const handleClear = () => {
    setImageBase64(null);
    setImagePreview(null);
    setUserNote('');
    setResult(null);
    setErrorMessage(null);
  };

  const handleAnalyze = async () => {
    if (!imageBase64) {
      setErrorMessage('Please upload or select an error screenshot first.');
      return;
    }

    setIsLoading(true);
    setErrorMessage(null);

    try {
      const resp = await assistantApi.explainImage(imageBase64, userNote);
      setResult(resp);
    } catch (err: any) {
      setErrorMessage('Analysis could not complete: ' + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  // Pre-set sample error scenarios for one-click testing
  const loadPresetSample = (title: string, note: string) => {
    // Generate a clean placeholder SVG canvas image
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="600" height="300" viewBox="0 0 600 300">
      <rect width="600" height="300" fill="#1e293b"/>
      <rect x="20" y="20" width="560" height="260" rx="12" fill="#0f172a" stroke="#ef4444" stroke-width="2"/>
      <circle cx="60" cy="60" r="16" fill="#ef4444"/>
      <text x="54" y="67" font-family="sans-serif" font-size="20" font-weight="bold" fill="#ffffff">!</text>
      <text x="95" y="68" font-family="sans-serif" font-size="22" font-weight="bold" fill="#f87171">${title}</text>
      <text x="95" y="110" font-family="sans-serif" font-size="15" fill="#94a3b8">An unexpected alert interrupted your application session.</text>
      <text x="95" y="140" font-family="sans-serif" font-size="14" fill="#cbd5e1">Error Code: ERR_REQUEST_FAILED_TRY_AGAIN</text>
      <rect x="95" y="180" width="120" height="40" rx="8" fill="#3b82f6"/>
      <text x="135" y="205" font-family="sans-serif" font-size="14" font-weight="bold" fill="#ffffff">Dismiss</text>
    </svg>`;
    const base64 = 'data:image/svg+xml;base64,' + btoa(svg);
    setImageBase64(base64);
    setImagePreview(base64);
    setUserNote(note);
    setResult(null);
  };

  return (
    <div
      onPaste={handlePaste}
      className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8"
    >
      {/* Page Title */}
      <div className="max-w-3xl space-y-2">
        <div className="inline-flex items-center gap-1.5 bg-purple-100 dark:bg-purple-950 text-purple-700 dark:text-purple-300 px-3 py-1 rounded-full text-xs font-bold border border-purple-200 dark:border-purple-800">
          <Sparkles className="w-3.5 h-3.5" />
          <span>Multimodal Vision AI Assistant</span>
        </div>
        <h1 className="text-3xl font-extrabold text-slate-900 dark:text-white">
          Screenshot & Error Explainer
        </h1>
        <p className="text-sm text-slate-600 dark:text-slate-300">
          Don't know what an error message means? Upload a photo or screenshot from your mobile or computer. We will explain it in simple words with step-by-step solutions.
        </p>
      </div>

      {/* Sensitive Data Privacy Warning Banner (Mandatory Requirement) */}
      <div className="bg-amber-50 dark:bg-amber-950/40 border-2 border-amber-300 dark:border-amber-800 rounded-2xl p-4 sm:p-5 flex items-start gap-3.5 shadow-sm">
        <div className="p-2 bg-amber-100 dark:bg-amber-900 text-amber-800 dark:text-amber-200 rounded-xl shrink-0 mt-0.5">
          <EyeOff className="w-5 h-5" />
        </div>
        <div className="space-y-1">
          <h4 className="text-sm font-bold text-amber-950 dark:text-amber-200 flex items-center gap-2">
            <span>Privacy First: Hide Sensitive Information Before Uploading</span>
          </h4>
          <p className="text-xs text-amber-900 dark:text-amber-300/90 leading-relaxed">
            Please make sure that <strong>passwords, OTPs, PINs, bank card numbers, CVVs, and Aadhaar numbers</strong> are not visible in your screenshot. Crop or scribble over private details to keep your account safe.
          </p>
        </div>
      </div>

      {/* Main Upload and Analysis Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Left Column: Upload & Preview */}
        <div className="lg:col-span-6 space-y-4">
          <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-sm space-y-4">
            <h3 className="font-bold text-sm text-slate-900 dark:text-white flex items-center justify-between">
              <span>Upload Error Screenshot</span>
              {imagePreview && (
                <button
                  onClick={handleClear}
                  className="text-xs text-rose-500 hover:underline flex items-center gap-1"
                >
                  <X className="w-3.5 h-3.5" /> Remove Photo
                </button>
              )}
            </h3>

            {/* Drop Zone */}
            {!imagePreview ? (
              <div
                onDragOver={(e) => e.preventDefault()}
                onDrop={handleDrop}
                className="border-2 border-dashed border-slate-300 dark:border-slate-700 hover:border-primary-500 dark:hover:border-primary-500 rounded-2xl p-8 text-center transition cursor-pointer bg-slate-50 dark:bg-slate-800/40"
              >
                <input
                  type="file"
                  id="screenshot-input"
                  accept="image/*"
                  onChange={(e) => e.target.files?.[0] && handleFileChange(e.target.files[0])}
                  className="hidden"
                />
                <label
                  htmlFor="screenshot-input"
                  className="cursor-pointer flex flex-col items-center justify-center space-y-3"
                >
                  <div className="w-14 h-14 rounded-2xl bg-primary-50 dark:bg-primary-950/80 text-primary-600 dark:text-primary-400 flex items-center justify-center shadow-inner">
                    <UploadCloud className="w-7 h-7" />
                  </div>
                  <div>
                    <p className="font-bold text-sm text-slate-800 dark:text-slate-200">
                      Click to choose image or drag & drop here
                    </p>
                    <p className="text-xs text-slate-500 mt-1">
                      PNG, JPG, WEBP up to 5MB (or press Ctrl+V to paste screenshot)
                    </p>
                  </div>
                </label>
              </div>
            ) : (
              <div className="relative rounded-xl overflow-hidden border border-slate-200 dark:border-slate-800 max-h-80 bg-slate-950 flex items-center justify-center">
                <img
                  src={imagePreview}
                  alt="Error Screenshot Preview"
                  className="max-h-80 w-auto object-contain"
                />
              </div>
            )}

            {/* Optional User Note */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                What were you trying to do when this happened? (Optional)
              </label>
              <input
                type="text"
                value={userNote}
                onChange={(e) => setUserNote(e.target.value)}
                placeholder="e.g. Trying to pay electricity bill on PhonePe, or opening an email attachment"
                className="w-full text-xs p-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-primary-500"
              />
            </div>

            {/* Error banner if file issues */}
            {errorMessage && (
              <div className="p-3 rounded-xl bg-rose-50 dark:bg-rose-950/60 border border-rose-200 dark:border-rose-900 text-rose-700 dark:text-rose-300 text-xs flex items-center gap-2">
                <AlertTriangle className="w-4 h-4 shrink-0" />
                <span>{errorMessage}</span>
              </div>
            )}

            {/* Analyze Action Button */}
            <button
              onClick={handleAnalyze}
              disabled={!imageBase64 || isLoading}
              className="w-full bg-primary-600 hover:bg-primary-700 disabled:opacity-40 disabled:cursor-not-allowed text-white font-bold py-3 rounded-xl text-sm flex items-center justify-center gap-2 shadow-md shadow-primary-500/20 transition cursor-pointer"
            >
              {isLoading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Analyzing screenshot with Vision AI...</span>
                </>
              ) : (
                <>
                  <Sparkles className="w-4 h-4" />
                  <span>Explain This Error</span>
                </>
              )}
            </button>
          </div>

          {/* Preset Example Buttons */}
          <div className="space-y-2">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider block">
              Or test with sample error scenarios:
            </span>
            <div className="flex flex-wrap gap-2">
              <button
                onClick={() => loadPresetSample('UPI Payment Failed', 'My bank debited the money but merchant says pending')}
                className="text-xs bg-white dark:bg-slate-800 px-3 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:border-primary-400"
              >
                💳 UPI Payment Failed
              </button>
              <button
                onClick={() => loadPresetSample('Wi-Fi Connected, No Internet', 'Wi-Fi has exclamation mark')}
                className="text-xs bg-white dark:bg-slate-800 px-3 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:border-primary-400"
              >
                📶 Wi-Fi No Internet
              </button>
              <button
                onClick={() => loadPresetSample('Storage Space Running Out', 'Cannot take photos or update WhatsApp')}
                className="text-xs bg-white dark:bg-slate-800 px-3 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:border-primary-400"
              >
                💾 Storage Full Alert
              </button>
            </div>
          </div>
        </div>

        {/* Right Column: AI Analysis Results */}
        <div className="lg:col-span-6">
          {result ? (
            <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-sm space-y-6">
              {/* Error Identified Header */}
              <div className="border-b border-slate-200 dark:border-slate-800 pb-4">
                <span className="text-[11px] font-bold uppercase tracking-wider bg-primary-100 dark:bg-primary-950 text-primary-700 dark:text-primary-300 px-2.5 py-1 rounded-full border border-primary-200 dark:border-primary-800">
                  Diagnosis Complete
                </span>
                <h3 className="text-xl font-bold text-slate-900 dark:text-white mt-2">
                  {result.errorTitle}
                </h3>
                <p className="text-sm text-slate-600 dark:text-slate-300 mt-1 leading-relaxed">
                  {result.plainExplanation}
                </p>
              </div>

              {/* Possible Causes */}
              <div className="space-y-2">
                <h4 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
                  <HelpCircle className="w-4 h-4 text-amber-500" />
                  <span>Why Did This Happen?</span>
                </h4>
                <ul className="space-y-1.5 text-xs text-slate-600 dark:text-slate-400">
                  {result.possibleCauses.map((cause, idx) => (
                    <li key={idx} className="flex items-start gap-2 bg-slate-50 dark:bg-slate-800/60 p-2.5 rounded-xl">
                      <span className="text-amber-500 font-bold">•</span>
                      <span>{cause}</span>
                    </li>
                  ))}
                </ul>
              </div>

              {/* Step-by-Step Fix */}
              <div className="space-y-2">
                <h4 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                  <span>How to Fix It (Step-by-Step)</span>
                </h4>
                <div className="space-y-2">
                  {result.stepByStepFix.map((fix, idx) => (
                    <div
                      key={idx}
                      className="text-xs p-3 rounded-xl bg-emerald-50/70 dark:bg-emerald-950/30 border border-emerald-200/80 dark:border-emerald-900/60 text-slate-800 dark:text-slate-200 leading-relaxed font-medium"
                    >
                      {fix}
                    </div>
                  ))}
                </div>
              </div>

              {/* Safety Precautions */}
              <div className="p-3.5 rounded-xl bg-slate-100 dark:bg-slate-800 text-xs text-slate-600 dark:text-slate-300 flex items-start gap-2 border border-slate-200 dark:border-slate-700">
                <ShieldAlert className="w-4 h-4 text-primary-500 shrink-0 mt-0.5" />
                <span>{result.safetyWarning}</span>
              </div>
            </div>
          ) : (
            <div className="h-full bg-slate-50 dark:bg-slate-900/40 rounded-2xl border-2 border-dashed border-slate-200 dark:border-slate-800 flex flex-col items-center justify-center p-8 text-center text-slate-400 space-y-3 min-h-[350px]">
              <ImageIcon className="w-12 h-12 text-slate-300 dark:text-slate-700" />
              <div className="max-w-sm">
                <p className="font-semibold text-sm text-slate-600 dark:text-slate-300">
                  No screenshot uploaded yet
                </p>
                <p className="text-xs text-slate-400 mt-1">
                  Upload an error image on the left, and our Vision AI will break down the problem in plain English and provide step-by-step instructions.
                </p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
