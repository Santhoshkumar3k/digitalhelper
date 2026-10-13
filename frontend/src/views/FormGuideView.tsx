import React, { useState, useEffect } from 'react';
import { documentApi } from '../services/api';
import { DocumentExplainResponse } from '../types';
import {
  FileText,
  CheckSquare,
  AlertCircle,
  HelpCircle,
  Info,
  Sparkles,
  Loader2,
  ChevronRight,
  ExternalLink,
} from 'lucide-react';

export const FormGuideView: React.FC = () => {
  const [selectedForm, setSelectedForm] = useState<string>('Passport Application');
  const [customInstructions, setCustomInstructions] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [guide, setGuide] = useState<DocumentExplainResponse | null>(null);

  const formTypes = [
    { id: 'Passport Application', label: 'Passport Application', category: 'Government ID' },
    { id: 'PAN Card Correction & New', label: 'PAN Card (Income Tax)', category: 'Tax & Identity' },
    { id: 'Aadhaar Card Update', label: 'Aadhaar Card Address/Mobile', category: 'Biometrics' },
    { id: 'Bank KYC & Account Opening', label: 'Bank Account & KYC', category: 'Banking' },
    { id: 'Driving License Online (Sarathi)', label: 'Driving License / Learner', category: 'Transport' },
    { id: 'College & Job Application', label: 'Job & Exam Application', category: 'Education' },
  ];

  useEffect(() => {
    loadFormGuide(selectedForm);
  }, [selectedForm]);

  const loadFormGuide = async (formType: string) => {
    setIsLoading(true);
    try {
      const resp = await documentApi.explain(formType, customInstructions);
      setGuide(resp);
    } catch (err: any) {
      alert('Failed to load form guide: ' + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCustomSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    loadFormGuide(selectedForm);
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Title */}
      <div className="max-w-3xl space-y-2">
        <div className="inline-flex items-center gap-1.5 bg-sky-100 dark:bg-sky-950 text-sky-700 dark:text-sky-300 px-3 py-1 rounded-full text-xs font-bold border border-sky-200 dark:border-sky-800">
          <FileText className="w-3.5 h-3.5" />
          <span>Paperwork & Application Guidance</span>
        </div>
        <h1 className="text-3xl font-extrabold text-slate-900 dark:text-white">
          Form & Document Guide
        </h1>
        <p className="text-sm text-slate-600 dark:text-slate-300">
          Confused by tricky form fields like "Given Name vs Surname", "ECR Status", or file upload rules? Select a form below for field-by-field guidance and document checklists.
        </p>
      </div>

      {/* Mandatory Disclaimer */}
      <div className="bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-800 rounded-2xl p-4 flex items-start gap-3">
        <Info className="w-5 h-5 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
        <p className="text-xs text-amber-900 dark:text-amber-200 leading-relaxed">
          <strong>Educational Checklist Only:</strong> DigitalHelper never automatically submits official forms or asks for your sensitive documents. Always verify current rules on the designated official government portal.
        </p>
      </div>

      {/* Form Selector Tabs */}
      <div className="flex flex-wrap gap-2">
        {formTypes.map((item) => (
          <button
            key={item.id}
            onClick={() => setSelectedForm(item.id)}
            className={`px-4 py-2.5 rounded-xl text-xs font-bold transition flex items-center gap-2 ${
              selectedForm === item.id
                ? 'bg-primary-600 text-white shadow-md shadow-primary-500/20'
                : 'bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 border border-slate-200 dark:border-slate-800'
            }`}
          >
            <span>{item.label}</span>
          </button>
        ))}
      </div>

      {/* Content Body */}
      {isLoading ? (
        <div className="p-16 flex flex-col items-center justify-center text-slate-400 space-y-3">
          <Loader2 className="w-8 h-8 animate-spin text-primary-500" />
          <p className="text-xs">Preparing document checklist and field guidance...</p>
        </div>
      ) : guide ? (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          {/* Left Column: Purpose & Checklist */}
          <div className="lg:col-span-5 space-y-6">
            {/* Purpose Card */}
            <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-sm space-y-3">
              <span className="text-[10px] font-bold uppercase tracking-wider text-primary-600 dark:text-primary-400 bg-primary-50 dark:bg-primary-950 px-2 py-0.5 rounded-full border border-primary-200 dark:border-primary-800">
                Official Purpose
              </span>
              <h3 className="text-xl font-bold text-slate-900 dark:text-white">
                {guide.formTitle}
              </h3>
              <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed">
                {guide.purpose}
              </p>
            </div>

            {/* Checklist */}
            <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-sm space-y-4">
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-900 dark:text-white flex items-center gap-2">
                <CheckSquare className="w-4 h-4 text-emerald-500" />
                <span>Required Documents Checklist</span>
              </h4>
              <div className="space-y-2">
                {guide.requiredDocumentsChecklist.map((doc, idx) => (
                  <div
                    key={idx}
                    className="p-3 rounded-xl bg-emerald-50/60 dark:bg-emerald-950/20 border border-emerald-200/60 dark:border-emerald-900/40 text-xs text-slate-800 dark:text-slate-200 flex items-start gap-2.5"
                  >
                    <span className="text-emerald-500 font-bold shrink-0">✓</span>
                    <span className="leading-relaxed">{doc}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Common Mistakes */}
            <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-sm space-y-3">
              <h4 className="text-xs font-bold uppercase tracking-wider text-rose-600 dark:text-rose-400 flex items-center gap-2">
                <AlertCircle className="w-4 h-4" />
                <span>Common Reasons for Rejection</span>
              </h4>
              <ul className="space-y-2 text-xs text-slate-600 dark:text-slate-400">
                {guide.commonRejectionReasons.map((reason, idx) => (
                  <li key={idx} className="flex items-start gap-2 bg-rose-50/50 dark:bg-rose-950/20 p-2.5 rounded-xl border border-rose-200/50 dark:border-rose-900/30">
                    <span className="text-rose-500 font-bold shrink-0">✕</span>
                    <span>{reason}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>

          {/* Right Column: Field-by-Field Breakdown & Steps */}
          <div className="lg:col-span-7 space-y-6">
            {/* Field-by-field guidance */}
            <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-sm space-y-4">
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-900 dark:text-white flex items-center gap-2">
                <HelpCircle className="w-4 h-4 text-primary-500" />
                <span>Tricky Fields Explained in Simple Language</span>
              </h4>
              <div className="space-y-3">
                {Object.entries(guide.fieldExplanations).map(([field, explanation], idx) => (
                  <div
                    key={idx}
                    className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 text-xs space-y-1"
                  >
                    <strong className="text-slate-900 dark:text-white text-xs block">
                      Field: "{field}"
                    </strong>
                    <p className="text-slate-600 dark:text-slate-300 leading-relaxed">
                      {explanation}
                    </p>
                  </div>
                ))}
              </div>
            </div>

            {/* Step-by-Step Filling Guide */}
            <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-sm space-y-3">
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-900 dark:text-white flex items-center gap-2">
                <Sparkles className="w-4 h-4 text-amber-500" />
                <span>Step-by-Step Procedure</span>
              </h4>
              <div className="space-y-2">
                {guide.stepByStepGuide.map((step, idx) => (
                  <div
                    key={idx}
                    className="p-3 rounded-xl bg-slate-100 dark:bg-slate-800 text-xs text-slate-800 dark:text-slate-200 leading-relaxed font-medium"
                  >
                    {step}
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
};
