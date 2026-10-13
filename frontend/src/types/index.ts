export interface User {
  id: number;
  email: string;
  fullName: string;
  role: string;
  createdAt: string;
}

export interface UserPreferences {
  preferredLanguage: string;
  highContrast: boolean;
  largeFont: boolean;
  theme: string;
  notifySecurityAlerts: boolean;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}

export interface AskResponse {
  conversationId: number;
  messageId: number;
  answer: string;
  followUpSuggestions: string[];
  category: string;
  aiGenerated: boolean;
}

export interface Message {
  id: number;
  role: 'user' | 'assistant' | 'system';
  content: string;
  imageUrl?: string;
  createdAt: string;
}

export interface ConversationSummary {
  id: number;
  title: string;
  category: string;
  createdAt: string;
  updatedAt: string;
  messageCount: number;
}

export interface ConversationDetail {
  id: number;
  title: string;
  category: string;
  createdAt: string;
  updatedAt: string;
  messages: Message[];
}

export interface ExplainImageResponse {
  errorTitle: string;
  plainExplanation: string;
  possibleCauses: string[];
  stepByStepFix: string[];
  safetyWarning: string;
  sensitiveDataDetected: boolean;
}

export interface TranslateResponse {
  originalText: string;
  translatedText: string;
  simplifiedText: string;
  targetLanguage: string;
  targetLanguageName: string;
  difficultTermsExplained: Record<string, string>;
  disclaimer: string;
}

export interface ScamCheckResponse {
  riskLevel: 'LOW_CONCERN' | 'SUSPICIOUS' | 'HIGH_RISK';
  riskScore: number;
  summary: string;
  warningFlags: string[];
  safeNextSteps: string[];
  officialAlternative: string;
  safetyDisclaimer: string;
}

export interface DocumentExplainResponse {
  formTitle: string;
  purpose: string;
  requiredDocumentsChecklist: string[];
  fieldExplanations: Record<string, string>;
  commonRejectionReasons: string[];
  stepByStepGuide: string[];
  disclaimer: string;
}

export interface SavedAnswer {
  id: number;
  title: string;
  originalQuestion: string;
  content: string;
  category: string;
  tags?: string;
  isFavorite: boolean;
  createdAt: string;
}

export interface Feedback {
  id: number;
  userEmail: string;
  rating: number;
  helpful: boolean;
  comments: string;
  feature: string;
  createdAt: string;
}

export interface AdminStats {
  totalUsers: number;
  totalConversations: number;
  totalMessages: number;
  totalScamChecks: number;
  totalSavedAnswers: number;
  totalFeedbacks: number;
  positiveFeedbackPercent: number;
  averageRating: number;
  highRiskScamsDetected: number;
  aiProvider: string;
  systemHealth: string;
}

export interface NetworkCallLog {
  id: string;
  timestamp: string;
  method: 'GET' | 'POST' | 'PATCH' | 'DELETE';
  endpoint: string;
  fullUrl: string;
  status: number;
  statusText: string;
  durationMs: number;
  requestHeaders: Record<string, string>;
  requestPayload?: any;
  responsePayload?: any;
  securityNotes: string;
}
