import {
  AuthResponse,
  User,
  UserPreferences,
  AskResponse,
  ConversationSummary,
  ConversationDetail,
  ExplainImageResponse,
  TranslateResponse,
  ScamCheckResponse,
  DocumentExplainResponse,
  SavedAnswer,
  Feedback,
  AdminStats,
  NetworkCallLog,
} from '../types';

const API_BASE_URL = typeof window !== 'undefined' && window.location.hostname
  ? `${window.location.protocol}//${window.location.hostname}:8080/api/v1`
  : 'http://localhost:8080/api/v1';

// Network Logger subscribers for UI Inspector (Swiggy / QuickBite style)
type NetworkLogListener = (logs: NetworkCallLog[]) => void;
let networkLogs: NetworkCallLog[] = [];
let networkListeners: NetworkLogListener[] = [];

export function subscribeToNetworkLogs(listener: NetworkLogListener) {
  networkListeners.push(listener);
  listener([...networkLogs]);
  return () => {
    networkListeners = networkListeners.filter((l) => l !== listener);
  };
}

export function clearNetworkLogs() {
  networkLogs = [];
  networkListeners.forEach((l) => l([]));
}

function recordNetworkCall(log: NetworkCallLog) {
  networkLogs = [log, ...networkLogs.slice(0, 49)]; // keep latest 50
  networkListeners.forEach((l) => l([...networkLogs]));
}

function getAuthHeader(): Record<string, string> {
  const token = localStorage.getItem('digitalhelper_token');
  if (token) {
    return { Authorization: `Bearer ${token}` };
  }
  return {};
}

async function request<T>(
  method: 'GET' | 'POST' | 'PATCH' | 'DELETE',
  endpoint: string,
  body?: any
): Promise<T> {
  const fullUrl = `${API_BASE_URL}${endpoint}`;
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    Accept: 'application/json',
    ...getAuthHeader(),
  };

  const startTime = performance.now();
  const id = Math.random().toString(36).substring(2, 9);

  let securityNotes = 'Secured: TLS/HTTPS Ready, CORS restricted';
  if (headers.Authorization) {
    securityNotes += ', JWT Bearer Token Attached (User Authenticated)';
  } else {
    securityNotes += ', Public / Guest Endpoint';
  }

  try {
    const fetchOptions: RequestInit = {
      method,
      headers,
    };
    if (body) {
      fetchOptions.body = JSON.stringify(body);
    }

    const response = await fetch(fullUrl, fetchOptions);
    const durationMs = Math.round(performance.now() - startTime);

    let json: any = null;
    const text = await response.text();
    if (text) {
      try {
        json = JSON.parse(text);
      } catch {
        json = text;
      }
    }

    const logEntry: NetworkCallLog = {
      id,
      timestamp: new Date().toLocaleTimeString(),
      method,
      endpoint,
      fullUrl,
      status: response.status,
      statusText: response.statusText,
      durationMs,
      requestHeaders: headers,
      requestPayload: body,
      responsePayload: json,
      securityNotes,
    };
    recordNetworkCall(logEntry);

    if (!response.ok) {
      const errorMsg = json?.message || `HTTP ${response.status}: ${response.statusText}`;
      throw new Error(errorMsg);
    }

    return (json?.data !== undefined ? json.data : json) as T;
  } catch (err: any) {
    const durationMs = Math.round(performance.now() - startTime);
    const logEntry: NetworkCallLog = {
      id,
      timestamp: new Date().toLocaleTimeString(),
      method,
      endpoint,
      fullUrl,
      status: 0,
      statusText: 'Network / CORS Error',
      durationMs,
      requestHeaders: headers,
      requestPayload: body,
      responsePayload: { error: err.message },
      securityNotes: 'Connection error: Ensure Spring Boot backend is running on :8080',
    };
    recordNetworkCall(logEntry);
    throw err;
  }
}

// Authentication APIs
export const authApi = {
  login: (email: string, password: string) =>
    request<AuthResponse>('POST', '/auth/login', { email, password }),

  register: (fullName: string, email: string, password: string) =>
    request<AuthResponse>('POST', '/auth/register', { fullName, email, password }),

  logout: () =>
    request<void>('POST', '/auth/logout'),
};

// User Profile & Preferences APIs
export const userApi = {
  getMe: () =>
    request<User>('GET', '/users/me'),

  getPreferences: () =>
    request<UserPreferences>('GET', '/users/me/preferences'),

  updatePreferences: (prefs: Partial<UserPreferences>) =>
    request<UserPreferences>('PATCH', '/users/me/preferences', prefs),

  deleteAccount: () =>
    request<void>('DELETE', '/users/me'),
};

// AI Assistant APIs
export const assistantApi = {
  ask: (prompt: string, conversationId?: number, language?: string, mode?: string, previousContext?: string) =>
    request<AskResponse>('POST', '/assistant/ask', { prompt, conversationId, language, mode, previousContext }),

  getConversations: () =>
    request<ConversationSummary[]>('GET', '/assistant/conversations'),

  getConversation: (id: number) =>
    request<ConversationDetail>('GET', `/assistant/conversations/${id}`),

  deleteConversation: (id: number) =>
    request<void>('DELETE', `/assistant/conversations/${id}`),

  explainImage: (imageBase64: string, userNote?: string, language?: string) =>
    request<ExplainImageResponse>('POST', '/assistant/explain-image', { imageBase64, userNote, language }),

  translate: (text: string, targetLanguage: string, mode?: string, depth?: string) =>
    request<TranslateResponse>('POST', '/assistant/translate', { text, targetLanguage, mode, depth }),

  getAiStatus: () =>
    request<{ configured: boolean; provider: string; providerName: string; model: string; maskedApiKey: string }>('GET', '/assistant/ai-status'),

  configureAi: (config: { provider?: string; apiKey: string; model?: string; apiUrl?: string }) =>
    request<{ configured: boolean; provider: string; providerName: string; model: string; maskedApiKey: string }>('POST', '/assistant/ai-configure', config),
};

// Scam & Safety Checker APIs
export const safetyApi = {
  checkMessage: (messageText: string, sourceUrl?: string) =>
    request<ScamCheckResponse>('POST', '/safety/check-message', { messageText, sourceUrl }),
};

// Document & Form Guide APIs
export const documentApi = {
  explain: (formType: string, instructionsOrTextFields?: string, query?: string) =>
    request<DocumentExplainResponse>('POST', '/documents/explain', { formType, instructionsOrTextFields, query }),
};

// Saved Answers (Bookmarks) APIs
export const savedAnswerApi = {
  getAll: (category?: string, query?: string) => {
    const params = new URLSearchParams();
    if (category && category !== 'All') params.append('category', category);
    if (query) params.append('query', query);
    const qs = params.toString() ? `?${params.toString()}` : '';
    return request<SavedAnswer[]>('GET', `/saved-answers${qs}`);
  },

  save: (data: { title: string; content: string; originalQuestion?: string; category?: string; tags?: string }) =>
    request<SavedAnswer>('POST', '/saved-answers', data),

  toggleFavorite: (id: number) =>
    request<SavedAnswer>('PATCH', `/saved-answers/${id}/favorite`),

  delete: (id: number) =>
    request<void>('DELETE', `/saved-answers/${id}`),
};

// Feedback APIs
export const feedbackApi = {
  submit: (data: { conversationId?: number; messageId?: number; rating: number; helpful: boolean; comments?: string; feature?: string }) =>
    request<Feedback>('POST', '/feedback', data),

  getMyFeedback: () =>
    request<Feedback[]>('GET', '/feedback/my'),
};

// Admin APIs
export const adminApi = {
  getStats: () =>
    request<AdminStats>('GET', '/admin/stats'),

  getAllFeedback: () =>
    request<Feedback[]>('GET', '/admin/feedback'),
};

// Health Check API
export const healthApi = {
  getHealth: () =>
    request<any>('GET', '/health'),
};
