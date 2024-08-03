import classNames from 'classnames'
import React, { useState } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faEnvelope } from '@fortawesome/free-regular-svg-icons'
import { faCheck } from '@fortawesome/free-solid-svg-icons'
import { useApiContext } from '../ApiProvider'
import { useI18n } from '../I18nProvider'

type MailingListFormProps = {
  onJoin?: () => void
  title?: React.ReactNode
  body?: React.ReactNode
}

/** shows a form for entering name and email to join the portal mailing list */
export function MailingListForm(props: MailingListFormProps) {
  const { onJoin, body, title } = props

  const { i18n } = useI18n()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [joined, setJoined] = useState(false)
  const Api = useApiContext()
  const containerClasses = classNames('d-flex', 'flex-column', 'align-items-center', 'mx-auto')

  const submit = (e: React.SyntheticEvent) => {
    e.preventDefault()
    Api.submitMailingListContact(name, email).then(() => {
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
    <h2 className="h4">{title ? title : i18n('joinMailingList')}</h2>
    {body ? body : <p>{i18n('mailingFormStayUpdated')}</p>}
    {!joined && <form onSubmit={submit} style={{ maxWidth: 300 }}>
      <input className="form-control my-3" size={30} style={inputStyle} type="text" placeholder={i18n('yourName')}
        value={name} onChange={e => setName(e.target.value)}/>
      <input className="form-control my-3" size={30} style={inputStyle} type="email" placeholder={i18n('yourEmail')}
        value={email} onChange={e => setEmail(e.target.value)}/>
      <button className="form-control btn-primary btn" disabled={!inputValid}>{i18n('join')}</button>
    </form>}
    {joined && <div className="text-center mt-2">
      <FontAwesomeIcon className="fa-lg p-2 rounded-circle" icon={faCheck} style={{
        color: 'var(--brand-color)',
        backgroundColor: 'var(--brand-color-shift-90)'
      }}/>
      <p>
        {i18n('thanksForJoining')}<br/>
      </p>

    </div>}
  </div>
}
