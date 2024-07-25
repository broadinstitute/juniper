import { faSmile } from '@fortawesome/free-regular-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import classNames from 'classnames'
import React, { useId, useState } from 'react'

import { StudyEnvironment } from 'api/api'

type StudyEnrollPasswordGateProps = {
  studyEnv: StudyEnvironment
  studyName: string
  onSubmitCorrectPassword: () => void
}

export const StudyEnrollPasswordGate = (props: StudyEnrollPasswordGateProps) => {
  const { studyEnv, studyName, onSubmitCorrectPassword } = props
  const { password: studyPassword } = studyEnv.studyEnvironmentConfig

  const inputId = useId()
  const [passwordError, setPasswordError] = useState<string>()
  const hasPasswordError = !!passwordError

  return (
    <div className="flex-grow-1" style={{ background: 'var(--bs-gray-100)' }}>
      <div className="mx-auto mt-5" style={{ width: 600, maxWidth: 'calc(100vw - 20px)' }}>
        <h1 className="h2 text-center mb-5">
          We are currently pre-enrolling for {studyName} with friends and family
        </h1>
        <div className="card text-center" style={{ background: 'var(--bs-white)' }}>
          <div className="card-body py-5">
            <FontAwesomeIcon className="fa-2x rounded-circle p-3 mb-2" icon={faSmile} style={{
              color: 'var(--brand-color)',
              backgroundColor: 'var(--brand-color-shift-90)'
            }}/>
            <div className="mx-auto" style={{ width: 300, maxWidth: '100%' }}>
              <form
                onSubmit={e => {
                  e.preventDefault()
                  const formData = new FormData(e.currentTarget)
                  const passwordInput = formData.get('password')
                  if (passwordInput === studyPassword) {
                    onSubmitCorrectPassword()
                  } else {
                    setPasswordError(!passwordInput ? 'Password is required.' : 'Password is incorrect.')
                  }
                }}
              >
                <label className="form-label h4 mb-4" htmlFor={inputId}>Enter soft launch code</label>
                <div className="mb-3">
                  <input
                    aria-describedby={`${inputId}-description`}
                    className={classNames('form-control', { 'is-invalid': hasPasswordError })}
                    id={inputId}
                    name="password"
                    type="text"
                    onChange={() => { setPasswordError(undefined) }}
                  />
                  <div
                    className={classNames('text-danger', { 'mt-1': hasPasswordError })}
                    id={`${inputId}-description`}
                    role="alert"
                  >
                    {passwordError}
                  </div>
                </div>
                <button className="btn btn-primary w-100 mb-4" type="submit">Continue</button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
