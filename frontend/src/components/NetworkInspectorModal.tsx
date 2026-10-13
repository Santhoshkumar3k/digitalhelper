import React, { useState, useEffect } from 'react';
import { subscribeToNetworkLogs, clearNetworkLogs } from '../services/api';
import { NetworkCallLog } from '../types';
import { Activity, ShieldCheck, Trash2, X, ChevronRight, CheckCircle2, AlertTriangle, ArrowUpDown } from 'lucide-react';

export const NetworkInspectorModal: React.FC = () => {
  const [logs, setLogs] = useState<NetworkCallLog[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const [selectedLog, setSelectedLog] = useState<NetworkCallLog | null>(null);
  const [filterMethod, setFilterMethod] = useState<string>('ALL');

  useEffect(() => {
    return subscribeToNetworkLogs((updatedLogs) => {
      setLogs(updatedLogs);
      if (updatedLogs.length > 0 && !selectedLog) {
        setSelectedLog(updatedLogs[0]);
      }
    });
  }, [selectedLog]);

  const filteredLogs = logs.filter((log) => {
    if (filterMethod === 'ALL') return true;
    return log.method === filterMethod;
  });

  return (
    <>
      {/* Floating Pill on Bottom Right - Swiggy/QuickBite API Security Badge */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="fixed bottom-4 right-4 z-50 flex items-center gap-2 bg-slate-900/95 dark:bg-slate-800/95 backdrop-blur-md text-white px-4 py-2.5 rounded-full shadow-2xl border border-slate-700 hover:border-primary-500 hover:scale-105 transition-all text-xs font-medium cursor-pointer"
        title="View live REST API Network calls & Security Audit"
      >
        <span className="relative flex h-2.5 w-2.5">
          <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
          <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-emerald-500"></span>
        </span>
        <Activity className="w-3.5 h-3.5 text-primary-400" />
        <span className="font-semibold text-slate-200">Network & API Inspector</span>
        <span className="bg-primary-600/80 text-white px-2 py-0.5 rounded-full text-[10px] font-bold">
          {logs.length}
        </span>
      </button>

      {/* Full Modal Drawer */}
      {isOpen && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex justify-end transition-opacity">
          <div className="w-full max-w-4xl bg-slate-950 text-slate-100 h-full shadow-2xl flex flex-col border-l border-slate-800 animate-in slide-in-from-right duration-200">
            {/* Header */}
            <div className="p-4 border-b border-slate-800 flex items-center justify-between bg-slate-900">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-primary-950/80 border border-primary-800 rounded-lg text-primary-400">
                  <Activity className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-white flex items-center gap-2">
                    REST API Activity & Security Monitor
                    <span className="text-[11px] bg-emerald-950 text-emerald-400 border border-emerald-800 px-2 py-0.5 rounded-full flex items-center gap-1">
                      <ShieldCheck className="w-3 h-3" /> QuickBite/Swiggy Security Pattern
                    </span>
                  </h3>
                  <p className="text-xs text-slate-400">
                    Inspecting real Spring Boot REST endpoints (<code className="text-primary-300">/api/v1/*</code>) & token headers
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <button
                  onClick={clearNetworkLogs}
                  className="p-1.5 hover:bg-slate-800 text-slate-400 hover:text-rose-400 rounded transition"
                  title="Clear Log History"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
                <button
                  onClick={() => setIsOpen(false)}
                  className="p-1.5 hover:bg-slate-800 text-slate-400 hover:text-white rounded transition"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
            </div>

            {/* Filter bar */}
            <div className="px-4 py-2.5 bg-slate-900/60 border-b border-slate-800 flex items-center justify-between text-xs">
              <div className="flex items-center gap-2">
                <span className="text-slate-400">Filter:</span>
                {['ALL', 'GET', 'POST', 'PATCH', 'DELETE'].map((m) => (
                  <button
                    key={m}
                    onClick={() => setFilterMethod(m)}
                    className={`px-2.5 py-1 rounded font-semibold text-[11px] transition ${
                      filterMethod === m
                        ? 'bg-primary-600 text-white'
                        : 'bg-slate-800 text-slate-300 hover:bg-slate-700'
                    }`}
                  >
                    {m}
                  </button>
                ))}
              </div>
              <span className="text-slate-400">
                Logged Calls: <strong className="text-white">{filteredLogs.length}</strong>
              </span>
            </div>

            {/* Main content: Master-Detail layout */}
            <div className="flex-1 flex overflow-hidden">
              {/* Left List of Endpoints */}
              <div className="w-1/2 border-r border-slate-800 overflow-y-auto divide-y divide-slate-800/60">
                {filteredLogs.length === 0 ? (
                  <div className="p-8 text-center text-slate-500 text-xs">
                    No network calls logged yet. Perform an action (ask a question, check a scam, translate, or login) to view live API calls.
                  </div>
                ) : (
                  filteredLogs.map((log) => {
                    const isSelected = selectedLog?.id === log.id;
                    const isSuccess = log.status >= 200 && log.status < 300;
                    return (
                      <div
                        key={log.id}
                        onClick={() => setSelectedLog(log)}
                        className={`p-3 cursor-pointer text-xs transition flex items-center justify-between hover:bg-slate-800/50 ${
                          isSelected ? 'bg-slate-800/80 border-l-4 border-primary-500' : ''
                        }`}
                      >
                        <div className="min-w-0 pr-2">
                          <div className="flex items-center gap-2 mb-1">
                            <span
                              className={`px-1.5 py-0.5 rounded text-[10px] font-bold ${
                                log.method === 'POST'
                                  ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                                  : log.method === 'GET'
                                  ? 'bg-sky-950 text-sky-400 border border-sky-800'
                                  : log.method === 'DELETE'
                                  ? 'bg-rose-950 text-rose-400 border border-rose-800'
                                  : 'bg-amber-950 text-amber-400 border border-amber-800'
                              }`}
                            >
                              {log.method}
                            </span>
                            <span className="font-mono text-slate-200 truncate font-medium">
                              {log.endpoint}
                            </span>
                          </div>
                          <div className="text-[10px] text-slate-400 flex items-center gap-2">
                            <span>{log.timestamp}</span>
                            <span>•</span>
                            <span>{log.durationMs}ms</span>
                          </div>
                        </div>

                        <div className="flex items-center gap-2 shrink-0">
                          <span
                            className={`px-1.5 py-0.5 rounded text-[10px] font-bold ${
                              isSuccess
                                ? 'bg-emerald-500/20 text-emerald-400'
                                : 'bg-rose-500/20 text-rose-400'
                            }`}
                          >
                            {log.status > 0 ? log.status : 'ERR'}
                          </span>
                          <ChevronRight className="w-3.5 h-3.5 text-slate-500" />
                        </div>
                      </div>
                    );
                  })
                )}
              </div>

              {/* Right Detail Pane */}
              <div className="w-1/2 overflow-y-auto p-4 text-xs space-y-4 bg-slate-900/40">
                {selectedLog ? (
                  <>
                    {/* Security & Audit Banner */}
                    <div className="p-3 rounded-lg bg-slate-800/80 border border-slate-700/80">
                      <div className="flex items-center gap-2 text-emerald-400 font-semibold mb-1">
                        <CheckCircle2 className="w-4 h-4" />
                        <span>Security & Privacy Compliance Check</span>
                      </div>
                      <p className="text-[11px] text-slate-300">{selectedLog.securityNotes}</p>
                    </div>

                    {/* General Info */}
                    <div>
                      <h4 className="font-semibold text-slate-300 uppercase tracking-wider text-[10px] mb-1">
                        Request Details
                      </h4>
                      <div className="bg-slate-950 p-2.5 rounded font-mono text-[11px] space-y-1 border border-slate-800">
                        <div>
                          <span className="text-slate-500">URL: </span>
                          <span className="text-primary-300 break-all">{selectedLog.fullUrl}</span>
                        </div>
                        <div>
                          <span className="text-slate-500">Method: </span>
                          <span className="text-emerald-400">{selectedLog.method}</span>
                        </div>
                        <div>
                          <span className="text-slate-500">Status: </span>
                          <span className={selectedLog.status >= 200 && selectedLog.status < 300 ? 'text-emerald-400' : 'text-rose-400'}>
                            {selectedLog.status} {selectedLog.statusText}
                          </span>
                        </div>
                        <div>
                          <span className="text-slate-500">Latency: </span>
                          <span className="text-amber-300">{selectedLog.durationMs} ms</span>
                        </div>
                      </div>
                    </div>

                    {/* Request Headers */}
                    <div>
                      <h4 className="font-semibold text-slate-300 uppercase tracking-wider text-[10px] mb-1">
                        Request Headers (Authorization Masked)
                      </h4>
                      <pre className="bg-slate-950 p-2.5 rounded font-mono text-[11px] text-slate-300 overflow-x-auto border border-slate-800 max-h-32">
                        {JSON.stringify(
                          {
                            ...selectedLog.requestHeaders,
                            Authorization: selectedLog.requestHeaders.Authorization
                              ? 'Bearer [SECURE_JWT_TOKEN_ACTIVE]'
                              : '[None - Public Endpoint]',
                          },
                          null,
                          2
                        )}
                      </pre>
                    </div>

                    {/* Request Body */}
                    {selectedLog.requestPayload && (
                      <div>
                        <h4 className="font-semibold text-slate-300 uppercase tracking-wider text-[10px] mb-1">
                          Request Payload
                        </h4>
                        <pre className="bg-slate-950 p-2.5 rounded font-mono text-[11px] text-slate-300 overflow-x-auto border border-slate-800 max-h-48">
                          {JSON.stringify(
                            // Mask password if login request
                            selectedLog.endpoint.includes('/auth') && selectedLog.requestPayload?.password
                              ? { ...selectedLog.requestPayload, password: '••••••••' }
                              : selectedLog.requestPayload,
                            null,
                            2
                          )}
                        </pre>
                      </div>
                    )}

                    {/* Response Body */}
                    <div>
                      <h4 className="font-semibold text-slate-300 uppercase tracking-wider text-[10px] mb-1">
                        Response Payload
                      </h4>
                      <pre className="bg-slate-950 p-2.5 rounded font-mono text-[11px] text-emerald-300 overflow-x-auto border border-slate-800 max-h-64">
                        {JSON.stringify(selectedLog.responsePayload, null, 2)}
                      </pre>
                    </div>
                  </>
                ) : (
                  <div className="text-slate-500 text-center py-12">
                    Select a request from the left column to view its headers, payload, and status.
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
};
