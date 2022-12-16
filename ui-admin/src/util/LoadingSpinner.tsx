import React from 'react'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDna } from '@fortawesome/free-solid-svg-icons'

/** show dna spinning indicator.
 * If no arguments or children are passed in, the spinner is rendered
 * Alternatively, children can be passed in, in which case either the spinner or the children
 * are rendered, contingent on the isLoading property
 */
export default function LoadingSpinner({ children, testId, isLoading }:
    { children?: React.ReactNode, testId?: string, isLoading?: boolean}) {
  const spinner = <FontAwesomeIcon icon={faDna} className="gene-load-spinner" data-testid={testId}/>
  if (!children && typeof(isLoading) === 'undefined') {
    return spinner
  }
  return <>
    {isLoading && spinner}
    {!isLoading && children }
  </>
}
