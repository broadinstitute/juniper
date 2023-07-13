import classNames from 'classnames'
import React, { useState } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faEnvelope } from '@fortawesome/free-regular-svg-icons'
import { faCheck } from '@fortawesome/free-solid-svg-icons'
import {useApiContext} from "../../participant/ApiProvider";

type MailingListFormProps = {
  onJoin?: () => void
}

/** shows a form for entering name and email to join the portal mailing list */
export function MailingListForm(props: MailingListFormProps) {
  const { onJoin } = props

  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [joined, setJoined] = useState(false)
  const { submitMailingListContact } = useApiContext()
  const containerClasses = classNames('d-flex', 'flex-column', 'align-items-center', 'mx-auto')

  const submit = (e: React.SyntheticEvent) => {
    e.preventDefault()
    submitMailingListContact(name, email).then(() => {
      setJoined(true)
      onJoin?.()
    })
  }
  // minimal validation - name not null and email has an @ and dots
  const inputValid = name && email && email.match(/.+@.+\..+/)
  const inputStyle = {
    background: '#f6f6f6'
  }
  return <div className={containerClasses}>
    <FontAwesomeIcon className="fa-2x p-3 rounded-circle mb-2" icon={faEnvelope} style={{
      color: 'var(--brand-color)',
      backgroundColor: 'var(--brand-color-shift-90)'
    }}/>
    <h2 className="h4">Join Mailing List</h2>
    <p>Stay updated with news about the study</p>
    {!joined && <form onSubmit={submit} style={{ maxWidth: 300 }}>
      <input className="form-control my-3" size={30} style={inputStyle} type="text" placeholder="Your name"
        value={name} onChange={e => setName(e.target.value)}/>
      <input className="form-control my-3" size={30} style={inputStyle} type="email" placeholder="Your email"
        value={email} onChange={e => setEmail(e.target.value)}/>
      <button className="form-control btn-primary btn" disabled={!inputValid}>Join</button>
    </form>}
    {joined && <div className="text-center mt-2">
      <FontAwesomeIcon className="fa-lg p-2 rounded-circle" icon={faCheck} style={{
        color: 'var(--brand-color)',
        backgroundColor: 'var(--brand-color-shift-90)'
      }}/>
      <p>
        Thanks for joining!<br/>
      </p>

    </div>}
  </div>
}
