import React, { ReactNode } from 'react'
import { StepStatus, stepStatusToIcon } from '../../kitCollectionUtils'

export const KitCollectionStepWrapper = ({ title, status, children, disabled }: {
    title: string, status: StepStatus, children: ReactNode, disabled?: boolean
}) => {
  return <div className="mb-3 rounded round-3 border border-1 p-3 bg-white"
    style={disabled ? { filter: 'brightness(33%)' } : {}}>
    <h2 className="d-flex align-items-center mb-3">
      { stepStatusToIcon(status) } {title}
    </h2>
    <div className='mb-2'>{children}</div>
  </div>
}
