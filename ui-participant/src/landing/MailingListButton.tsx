import React, { useId } from 'react'

import { MailingListModal } from './MailingListModal'

type MailingListButtonProps = JSX.IntrinsicElements['button']

export const MailingListButton = (props: MailingListButtonProps) => {
  const modalId = useId()

  return (
    <>
      <button
        {...props}
        data-bs-toggle="modal"
        data-bs-target={`#${CSS.escape(modalId)}`}
      />

      <MailingListModal id={modalId} />
    </>
  )
}
