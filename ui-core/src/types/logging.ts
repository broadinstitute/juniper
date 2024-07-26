export type LogEvent = {
    id?: string,
    eventType: 'ERROR' | 'ACCESS' | 'EVENT' | 'STATS' | 'INFO'
    eventName: string,
    stackTrace?: string,
    eventDetail?: string,
    eventSource?: string,
    studyShortcode?: string,
    portalShortcode?: string,
    environmentName?: string,
    enrolleeShortcode?: string,
    operatorId?: string,
    createdAt?: number
}
