import React, { useRef } from 'react'

import MailingListForm from './MailingListForm'

type MailingListModalProps = {
  id: string
}

/** display a modal prompting user to join mailing list */
export const MailingListModal = (props: MailingListModalProps) => {
  const { id } = props
  const closeButtonRef = useRef<HTMLButtonElement>(null)

  return (
    <div
      className="modal fade"
      id={id}
      tabIndex={-1}
      aria-label="Join mailing list"
      aria-hidden="true"
    >
      <div className="modal-dialog">
        <div className="modal-content">
          <div className="modal-body py-5">
            <button
              ref={closeButtonRef}
              aria-label="Close"
              className="btn-close"
              data-bs-dismiss="modal"
              style={{
                position: 'absolute',
                top: 'calc(0.5 * var(--bs-modal-header-padding-y))',
                right: 'calc(0.5 * var(--bs-modal-header-padding-x))',
                padding: 'calc(var(--bs-modal-header-padding-y) * .5) calc(var(--bs-modal-header-padding-x) * .5)'
              }}
            />
            <MailingListForm onJoin={() => { closeButtonRef.current?.focus() }} />
          </div>
        </div>
      </div>
    </div>
  )
}
