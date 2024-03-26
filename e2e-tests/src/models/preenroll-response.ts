export interface PreEnrollAnswer {
  questionStableId: string;
  stringValue: string;
  viewedLanguage: string;
}

export interface PreEnrollPayload {
  answers: PreEnrollAnswer[];
  qualified: boolean;
  resumeData: string;
  studyEnvironmentId: string;
  surveyId: string;
}

export interface PreEnrollQuestionData {
  createdAt: number;
  lastUpdatedAt:number;
  questionStableId: string;
  surveyVersion: number;
  viewedLanguage: string;
  stringValue?: string;
  booleanValue?: boolean;
}

export interface PreEnrollResponse {
  id: string;
  createdAt: number;
  lastUpdatedAt: number;
  surveyId: string;
  studyEnvironmentId: string;
  qualified: boolean;
  fullData: string;
  //fullData: PreEnrollQuestionData[];
}
