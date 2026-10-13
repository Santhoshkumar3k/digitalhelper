import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Sun, Moon, Type, ShieldAlert } from 'lucide-react';

export const AccessibilityBar: React.FC = () => {
  const {
    fontSize,
    setFontSize,
    darkMode,
    setDarkMode,
    t,
  } = useAuth();

  return (
    <aside
      aria-label="Accessibility settings"
      className="bg-slate-100 dark:bg-slate-900 text-slate-900 dark:text-slate-100 text-xs py-1.5 px-4 sm:px-8 border-b-2 border-slate-300 dark:border-slate-800 transition-colors shadow-2xs"
    >
      <div className="w-full flex items-center justify-between gap-3">
        {/* Scam Safety Reminder Banner */}
        <div className="flex items-center gap-2 text-slate-900 dark:text-amber-300 font-bold text-xs sm:text-sm">
          <span className="w-2 h-2 rounded-full bg-orange-500 animate-ping shrink-0" />
          <ShieldAlert className="w-4 h-4 text-orange-600 dark:text-amber-400 shrink-0" />
          <span className="truncate max-w-[260px] sm:max-w-none">{t.safetyAlert}</span>
        </div>

        {/* Right Corner Controls: Professional Text Size + Theme Change Button */}
        <div className="flex items-center gap-2 sm:gap-3 shrink-0">
          {/* Professional Segmented Text Size Control (A and A+) */}
          <div className="flex items-center bg-white dark:bg-slate-800 rounded-full border-2 border-slate-300 dark:border-slate-700 p-0.5 shadow-2xs">
            <span className="text-[11px] font-black text-slate-700 dark:text-slate-300 pl-2 pr-1 hidden sm:flex items-center gap-1">
              <Type className="w-3.5 h-3.5 text-orange-500 dark:text-orange-400" />
              <span>{t.textSize}</span>
            </span>
            <div className="flex items-center gap-0.5">
              <button
                onClick={() => setFontSize('normal')}
                className={`px-2.5 py-0.5 text-xs font-black rounded-full transition-all cursor-pointer ${
                  fontSize === 'normal'
                    ? 'bg-orange-500 text-white shadow-xs'
                    : 'text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700'
                }`}
                title="Standard Text Size (A)"
              >
                A
              </button>
              <button
                onClick={() => setFontSize('large')}
                className={`px-2.5 py-0.5 text-xs font-black rounded-full transition-all cursor-pointer ${
                  fontSize === 'large'
                    ? 'bg-orange-500 text-white shadow-xs'
                    : 'text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700'
                }`}
                title="Large Text Size (A+)"
              >
                A+
              </button>
            </div>
          </div>

          {/* Theme Change Button placed on top-right corner */}
          <button
            onClick={() => setDarkMode(!darkMode)}
            className="flex items-center gap-1.5 px-3 py-1 rounded-full border-2 border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 hover:border-orange-500 hover:text-orange-600 transition shadow-2xs cursor-pointer text-xs font-black"
            title={darkMode ? 'Switch to Bright Theme' : 'Switch to Dark Theme'}
            aria-label="Toggle Theme"
          >
            {darkMode ? (
              <>
                <Sun className="w-3.5 h-3.5 text-amber-400" />
                <span className="hidden sm:inline">Bright</span>
              </>
            ) : (
              <>
                <Moon className="w-3.5 h-3.5 text-orange-500" />
                <span className="hidden sm:inline">Dark</span>
              </>
            )}
          </button>
        </div>
      </div>
    </aside>
  );
};
