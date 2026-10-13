import React, { useState } from 'react';
import { safetyApi } from '../services/api';
import { ScamCheckResponse } from '../types';
import {
  ShieldAlert,
  ShieldCheck,
  AlertTriangle,
  ExternalLink,
  PhoneCall,
  CheckCircle2,
  Copy,
  Check,
  HelpCircle,
  Loader2,
  ArrowRight,
  Sparkles,
  Lock,
} from 'lucide-react';

export const ScamCheckerView: React.FC = () => {
  const [messageText, setMessageText] = useState<string>('');
  const [sourceUrl, setSourceUrl] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [result, setResult] = useState<ScamCheckResponse | null>(null);
  const [copied, setCopied] = useState<boolean>(false);

  const handleCheck = async () => {
    if (!messageText.trim()) return;
    setIsLoading(true);
    setResult(null);

    try {
      const resp = await safetyApi.checkMessage(messageText.trim(), sourceUrl.trim() || undefined);
      setResult(resp);
    } catch (err: any) {
      alert('Safety check could not be completed: ' + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCopyReport = () => {
    if (!result) return;
    const text = `DigitalHelper Scam Audit Report:\nRisk Level: ${result.riskLevel} (${result.riskScore}/100)\nSummary: ${result.summary}\nRed Flags:\n${result.warningFlags.map(f => `- ${f}`).join('\n')}\nRecommended Steps:\n${result.safeNextSteps.map(s => `- ${s}`).join('\n')}`;
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const loadPreset = (msg: string, url: string = '') => {
    setMessageText(msg);
    setSourceUrl(url);
    setResult(null);
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Title */}
      <div className="max-w-3xl space-y-2">
        <div className="inline-flex items-center gap-1.5 bg-amber-100 dark:bg-amber-950 text-amber-800 dark:text-amber-300 px-3 py-1 rounded-full text-xs font-bold border border-amber-200 dark:border-amber-800">
          <ShieldAlert className="w-3.5 h-3.5" />
          <span>Fraud & Phishing Defense</span>
        </div>
        <h1 className="text-3xl font-extrabold text-slate-900 dark:text-white">
          Scam & Fake Message Checker
        </h1>
        <p className="text-sm text-slate-600 dark:text-slate-300">
          Received a suspicious SMS, WhatsApp message, email, or unverified link? Paste it below to identify urgency traps, impersonation, and hidden threats before you click or reply.
        </p>
      </div>

      {/* Strict Security Promise Banner */}
      <div className="bg-slate-900 text-slate-200 rounded-2xl p-4 sm:p-5 border border-slate-800 flex flex-col sm:flex-row items-center justify-between gap-4 shadow-sm">
        <div className="flex items-center gap-3">
          <div className="p-2.5 bg-slate-800 text-emerald-400 rounded-xl shrink-0">
            <Lock className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-xs font-bold text-white uppercase tracking-wider">
              Safety Isolation Guarantee
            </h4>
            <p className="text-xs text-slate-400">
              Submitted URLs are <strong>never automatically opened or executed</strong>. DigitalHelper never asks for or stores passwords, OTPs, or banking details.
            </p>
          </div>
        </div>

        <div className="shrink-0 flex items-center gap-2 bg-slate-800/90 px-3 py-2 rounded-xl text-xs text-amber-300 border border-slate-700">
          <PhoneCall className="w-4 h-4 text-amber-400" />
          <span>India Cybercrime Helpline: <strong>1930</strong></span>
        </div>
      </div>

      {/* Main Grid: Input Form & Results */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Left Column: Input Form */}
        <div className="lg:col-span-6 space-y-4">
          <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-sm space-y-4">
            <h3 className="font-bold text-sm text-slate-900 dark:text-white">
              Paste Suspicious Message or Notification
            </h3>

            <div>
              <textarea
                rows={6}
                value={messageText}
                onChange={(e) => setMessageText(e.target.value)}
                placeholder="Paste the SMS, WhatsApp forward, or email body here (e.g. 'Dear customer, your electricity will be disconnected tonight at 9:30 PM due to unpaid bill...')"
                className="w-full text-xs sm:text-sm p-3.5 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-primary-500 resize-none leading-relaxed"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                Website Link / URL in Message (Optional)
              </label>
              <input
                type="text"
                value={sourceUrl}
                onChange={(e) => setSourceUrl(e.target.value)}
                placeholder="e.g. http://bit.ly/update-kyc or https://sbi-unblock.xyz"
                className="w-full text-xs p-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-primary-500"
              />
            </div>

            <button
              onClick={handleCheck}
              disabled={!messageText.trim() || isLoading}
              className="w-full bg-primary-600 hover:bg-primary-700 disabled:opacity-40 disabled:cursor-not-allowed text-white font-bold py-3 rounded-xl text-sm flex items-center justify-center gap-2 shadow-md shadow-primary-500/20 transition cursor-pointer"
            >
              {isLoading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Scanning patterns & security red flags...</span>
                </>
              ) : (
                <>
                  <ShieldCheck className="w-4 h-4" />
                  <span>Analyze Message for Safety</span>
                </>
              )}
            </button>
          </div>

          {/* Preset Real-World Scams to Test */}
          <div className="space-y-2">
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider block">
              Try testing common scam types:
            </span>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              <button
                onClick={() =>
                  loadPreset(
                    'Dear consumer, your electricity power will be disconnected tonight at 9:30 PM from the electricity office because your previous month bill was not updated. Please immediately contact our officer Mr. Sharma at 9876543210.',
                    ''
                  )
                }
                className="text-left text-xs bg-white dark:bg-slate-800 p-2.5 rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:border-amber-400"
              >
                ⚡ <strong>Electricity Bill Cutoff Threat</strong>
              </button>

              <button
                onClick={() =>
                  loadPreset(
                    'Dear SBI user, your NetBanking account will be suspended within 24 hours due to pending KYC. Click http://bit.ly/sbi-kyc-verify to update Aadhaar and avoid account closure.',
                    'http://bit.ly/sbi-kyc-verify'
                  )
                }
                className="text-left text-xs bg-white dark:bg-slate-800 p-2.5 rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:border-rose-400"
              >
                🏦 <strong>Bank Account Block Phishing</strong>
              </button>

              <button
                onClick={() =>
                  loadPreset(
                    'Congratulations! You won ₹25,00,000 in KBC Lucky Draw 2026. Scan this QR code and enter your UPI PIN to claim money directly into your bank account.',
                    ''
                  )
                }
                className="text-left text-xs bg-white dark:bg-slate-800 p-2.5 rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:border-rose-400"
              >
                🎁 <strong>UPI QR Lottery Scam</strong>
              </button>

              <button
                onClick={() =>
                  loadPreset(
                    'FedEx: Your parcel #IN9821 has been held at customs duty. Pay ₹450 release fee via this link to deliver today: http://fedex-customs.top',
                    'http://fedex-customs.top'
                  )
                }
                className="text-left text-xs bg-white dark:bg-slate-800 p-2.5 rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:border-amber-400"
              >
                📦 <strong>Fake Courier Customs Fee</strong>
              </button>
            </div>
          </div>
        </div>

        {/* Right Column: Safety Verdict & Red Flags */}
        <div className="lg:col-span-6">
          {result ? (
            <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-sm space-y-6">
              {/* Risk Level Meter Banner */}
              <div
                className={`p-4 rounded-xl border flex items-center justify-between ${
                  result.riskLevel === 'HIGH_RISK'
                    ? 'bg-rose-50 dark:bg-rose-950/40 border-rose-300 dark:border-rose-900 text-rose-900 dark:text-rose-200'
                    : result.riskLevel === 'SUSPICIOUS'
                    ? 'bg-amber-50 dark:bg-amber-950/40 border-amber-300 dark:border-amber-900 text-amber-900 dark:text-amber-200'
                    : 'bg-emerald-50 dark:bg-emerald-950/40 border-emerald-300 dark:border-emerald-900 text-emerald-900 dark:text-emerald-200'
                }`}
              >
                <div className="flex items-center gap-3">
                  {result.riskLevel === 'HIGH_RISK' ? (
                    <ShieldAlert className="w-8 h-8 text-rose-600 dark:text-rose-400 shrink-0" />
                  ) : result.riskLevel === 'SUSPICIOUS' ? (
                    <AlertTriangle className="w-8 h-8 text-amber-600 dark:text-amber-400 shrink-0" />
                  ) : (
                    <ShieldCheck className="w-8 h-8 text-emerald-600 dark:text-emerald-400 shrink-0" />
                  )}
                  <div>
                    <span className="text-[11px] font-bold uppercase tracking-wider block">
                      Assessed Threat Level
                    </span>
                    <h3 className="text-lg font-black">
                      {result.riskLevel === 'HIGH_RISK'
                        ? 'HIGH RISK — Likely Fraud / Scam'
                        : result.riskLevel === 'SUSPICIOUS'
                        ? 'SUSPICIOUS — Exercise Caution'
                        : 'LOW CONCERN — No Obvious Red Flags'}
                    </h3>
                  </div>
                </div>

                <div className="text-right">
                  <span className="text-2xl font-black">{result.riskScore}</span>
                  <span className="text-xs text-slate-500">/100</span>
                  <span className="block text-[10px] font-semibold uppercase text-slate-500">Risk Score</span>
                </div>
              </div>

              {/* Summary Description */}
              <div className="p-3.5 bg-slate-50 dark:bg-slate-800 rounded-xl text-xs text-slate-800 dark:text-slate-200 leading-relaxed font-medium">
                {result.summary}
              </div>

              {/* Detected Warning Flags */}
              <div className="space-y-2">
                <h4 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
                  <AlertTriangle className="w-4 h-4 text-rose-500" />
                  <span>Warning Signs Detected:</span>
                </h4>
                <div className="space-y-2">
                  {result.warningFlags.map((flag, idx) => (
                    <div
                      key={idx}
                      className="text-xs p-3 rounded-xl bg-rose-50/70 dark:bg-rose-950/20 border border-rose-200/70 dark:border-rose-900/40 text-slate-800 dark:text-slate-200 leading-relaxed"
                    >
                      <strong className="text-rose-600 dark:text-rose-400 block mb-0.5">⚠️ Red Flag #{idx + 1}</strong>
                      {flag}
                    </div>
                  ))}
                </div>
              </div>

              {/* Safe Next Steps */}
              <div className="space-y-2">
                <h4 className="text-xs font-bold text-slate-800 dark:text-slate-200 uppercase tracking-wider flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                  <span>What You Should Do (Safe Next Steps):</span>
                </h4>
                <ul className="space-y-1.5 text-xs text-slate-700 dark:text-slate-300">
                  {result.safeNextSteps.map((step, idx) => (
                    <li key={idx} className="flex items-start gap-2 bg-emerald-50/50 dark:bg-emerald-950/20 p-2.5 rounded-xl border border-emerald-200/50 dark:border-emerald-900/30">
                      <span className="text-emerald-500 font-bold shrink-0">✓</span>
                      <span>{step}</span>
                    </li>
                  ))}
                </ul>
              </div>

              {/* Verified Alternative */}
              <div className="p-3.5 rounded-xl bg-slate-100 dark:bg-slate-800 text-xs text-slate-600 dark:text-slate-300 space-y-1">
                <strong className="text-slate-900 dark:text-white block">Official Verification Alternative:</strong>
                <p>{result.officialAlternative}</p>
              </div>

              {/* Copy Report Button */}
              <button
                onClick={handleCopyReport}
                className="w-full py-2.5 rounded-xl border border-slate-200 dark:border-slate-700 text-xs font-bold text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 flex items-center justify-center gap-2 transition"
              >
                {copied ? <Check className="w-4 h-4 text-emerald-500" /> : <Copy className="w-4 h-4" />}
                <span>{copied ? 'Report Copied to Clipboard' : 'Copy Safety Report'}</span>
              </button>

              <p className="text-[11px] text-slate-400 italic text-center">
                {result.safetyDisclaimer}
              </p>
            </div>
          ) : (
            <div className="h-full bg-slate-50 dark:bg-slate-900/40 rounded-2xl border-2 border-dashed border-slate-200 dark:border-slate-800 flex flex-col items-center justify-center p-8 text-center text-slate-400 space-y-3 min-h-[350px]">
              <ShieldCheck className="w-12 h-12 text-slate-300 dark:text-slate-700" />
              <div className="max-w-sm">
                <p className="font-semibold text-sm text-slate-600 dark:text-slate-300">
                  Ready to check messages
                </p>
                <p className="text-xs text-slate-400 mt-1">
                  Paste any SMS, WhatsApp text, or email on the left to verify if it contains phishing links, false urgency, or credential theft attempts.
                </p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
