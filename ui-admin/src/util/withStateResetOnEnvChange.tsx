import React, { ComponentType, useEffect, useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'

export const withStateResetOnEnvChange = <T, >(WrappedComponent: ComponentType<T>) => {
  const WithStateResetOnEnvChange = (props: T & { studyEnvContext: StudyEnvContextT }) => {
    const { studyEnvContext } = props
    const [key, setKey] = useState(studyEnvContext.currentEnvPath)

    useEffect(() => {
      setKey(studyEnvContext.currentEnvPath)
    }, [studyEnvContext.currentEnvPath])

    return <WrappedComponent key={key} {...props} />
  }

  WithStateResetOnEnvChange.displayName =
    `WithStateResetOnEnvChange(${WrappedComponent.displayName || WrappedComponent.name || 'Component'})`

  return WithStateResetOnEnvChange
}
