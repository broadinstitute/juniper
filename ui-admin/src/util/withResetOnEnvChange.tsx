import React, { ComponentType, useEffect, useState } from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'

export const withResetOnEnvChange = <T, >(WrappedComponent: ComponentType<T>) => {
  const WithResetOnEnvChange = (props: T & { studyEnvContext: StudyEnvContextT }) => {
    const { studyEnvContext } = props
    const [key, setKey] = useState(studyEnvContext.currentEnvPath)

    useEffect(() => {
      setKey(studyEnvContext.currentEnvPath)
    }, [studyEnvContext.currentEnvPath])

    return <WrappedComponent key={key} {...props} />
  }

  WithResetOnEnvChange.displayName =
    `WithResetOnEnvChange(${WrappedComponent.displayName || WrappedComponent.name || 'Component'})`

  return WithResetOnEnvChange
}
