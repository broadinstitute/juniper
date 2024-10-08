export type KitType = {
    id: string,
    name: string,
    displayName: string,
    description: string
}

export type KitRequestStatus =
    'NEW' |
    'CREATED' |
    'QUEUED' |
    'SENT' |
    'COLLECTED_BY_STAFF' |
    'RECEIVED' |
    'ERRORED' |
    'DEACTIVATED' |
    'UNKNOWN'

export type KitRequest = {
    id: string,
    createdAt: number,
    kitType: KitType,
    distributionMethod: string,
    status: KitRequestStatus,
    sentToAddress: string,
    labeledAt?: number,
    sentAt?: number,
    receivedAt?: number,
    trackingNumber?: string,
    returnTrackingNumber?: string,
    kitLabel?: string,
    errorMessage?: string,
    details?: string,
    enrolleeShortcode?: string,
    skipAddressValidation: boolean
}
