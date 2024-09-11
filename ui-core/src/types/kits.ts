export type KitType = {
    id: string,
    name: string,
    displayName: string,
    description: string
}

export type KitRequest = {
    id: string,
    createdAt: number,
    kitType: KitType,
    distributionMethod: string,
    status: string,
    sentToAddress: string,
    labeledAt?: number,
    sentAt?: number,
    receivedAt?: number,
    trackingNumber?: string,
    returnTrackingNumber?: string,
    kitBarcode?: string,
    errorMessage?: string,
    details?: string,
    enrolleeShortcode?: string,
    skipAddressValidation: boolean
}
