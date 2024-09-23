import QRCode from 'react-qr-code'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faUser } from '@fortawesome/free-solid-svg-icons'
import React from 'react'

export const EnrolleeShortcodeQR = ({ shortcode }: { shortcode: string }) => {
  return (
    <div className="d-flex flex-column align-items-center pb-5">
      <QRCode value={shortcode} size={300}
        className={'m-5 p-4 mb-0 border rounded-3 shadow-lg'} aria-label={'shortcode-qr'}/>
      <div className={'border rounded-bottom-3 border-top-0 p-2 bg-white shadow-lg'}>
        <FontAwesomeIcon icon={faUser}/> {shortcode}
      </div>
    </div>
  )
}
