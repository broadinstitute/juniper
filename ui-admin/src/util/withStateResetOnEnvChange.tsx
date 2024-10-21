import React, { ComponentType } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'

export const withStateResetOnEnvChange = <T, >(WrappedComponent: ComponentType<T>) => {
  const WithStateResetOnEnvChange = (props: T & { studyEnvContext: StudyEnvContextT }) => {
    const { studyEnvContext } = props
    return <WrappedComponent key={studyEnvContext.currentEnvPath} {...props} />
  }

  WithStateResetOnEnvChange.displayName =
    `WithStateResetOnEnvChange(${WrappedComponent.displayName || WrappedComponent.name || 'Component'})`

  return WithStateResetOnEnvChange
}
