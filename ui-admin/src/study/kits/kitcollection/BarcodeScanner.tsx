import { IDetectedBarcode, IScannerProps, Scanner } from '@yudiel/react-qr-scanner'
import React from 'react'

export const BarcodeScanner = ({ expectedFormats, onSuccess, onError }: {
  expectedFormats: IScannerProps['formats']
  onSuccess: (result: IDetectedBarcode) => void,
  onError: (error: string) => void
}) => {
  return <div style={{ width: '80%', height: '80%' }}>
    <Scanner
      formats={expectedFormats}
      components={{ audio: false }}
      onScan={detectedCodes => {
        if (detectedCodes.length > 1) {
          onError('Multiple barcodes detected. Please scan again with only one barcode visible.')
        } else if (detectedCodes.length === 1) {
          onSuccess(detectedCodes[0])
        } else {
          onError('No barcode detected. Please try again.')
        }
      }}/>
  </div>
}
