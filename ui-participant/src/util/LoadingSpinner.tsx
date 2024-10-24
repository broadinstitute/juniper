import classNames from 'classnames'
import React, { CSSProperties, useEffect, useState } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDna } from '@fortawesome/free-solid-svg-icons/faDna'

import { DelayedRender } from './DelayedRender'

type LoadingSpinnerProps = {
  className?: string
  style?: CSSProperties
  testId?: string
}

/** Spinning DNA icon */
export const LoadingSpinner = (props: LoadingSpinnerProps) => {
  const { className, style, testId } = props
  return (
    <FontAwesomeIcon
      icon={faDna}
      className={classNames('gene-load-spinner', className)}
      data-testid={testId}
      style={style}
    />
  )
}

/** Show an overlay with a loading page over the full page.  If the page takes >150ms to load,
  a. loading spinner will be displayed */
export const PageLoadingIndicator = () => {
  // Render backdrop then add "show" class for transition on opacity.
  const [showBackdrop, setShowBackdrop] = useState(false)
  useEffect(() => {
    setTimeout(() => {
      setShowBackdrop(true)
    }, 0)
  }, [])

  return (
    <DelayedRender delay={150}>
      <div
        className={classNames('modal-backdrop fade', { show: showBackdrop })}
        style={{ '--bs-backdrop-opacity': 0.15 } as CSSProperties}
      />
      <div className="modal d-flex justify-content-center align-items-center">
        <LoadingSpinner style={{ height: 50 }} />
      </div>
    </DelayedRender>
  )
}
