import React, { ErrorInfo } from 'react'

type ErrorState = {
  error: Error | null,
  info: ErrorInfo | null
}

type ErrorProps = {
  children: React.ReactNode
}

/**
 * See https://reactjs.org/docs/error-boundaries.html
 * note that this must be a class component as hooks do not support componentDidCatch yet.
 *
 * adapted from
 * https://github.com/broadinstitute/single_cell_portal_core/blob/development/app/javascript/lib/ErrorBoundary.jsx
 */
export default class ErrorBoundary extends React.Component<ErrorProps, ErrorState> {
  /** initialize to a non-error state */
  constructor(props: ErrorProps) {
    super(props)
    this.state = { error: null, info: null }
  }

  /** log an error, and then update the display to show the error */
  componentDidCatch(error: Error, info: ErrorInfo) {
    // TODO log error to Mixpanel
    // TODO log error to Sentry
    this.setState({ error, info })
  }

  /** show an error if one exists, otherwise show the component */
  render() {
    if (this.state.error) {
      return (
        <div className="alert-danger text-center error-boundary">
          <span className="font-italic ">Something went wrong.</span><br/>
          <span>
            Please try reloading the page. If this error persists, or you require assistance, please
            contact support and include the error text below.
          </span>
          <pre>
            {this.state.error.message}
            {this.state.info?.componentStack}
          </pre>
        </div>
      )
    }

    return this.props.children
  }
}

/** HOC for wrapping arbitrary components in error boundaries */
export function withErrorBoundary<Props extends object>(Component: React.ComponentType<Props>) {
  return function SafeWrappedComponent(props: Props) {
    return (
      <ErrorBoundary>
        <Component {...props} />
      </ErrorBoundary>
    )
  }
}
