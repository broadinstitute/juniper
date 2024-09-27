import React, {
  lazy,
  Suspense
} from 'react'
import { JsonEditorProps } from 'util/json/JsonEditor'
import LoadingSpinner from 'util/LoadingSpinner'


const JsonEditor = lazy(() => import('./JsonEditor'))

/**
 * Antlr is a fairly large library, and it's only needed for specific workflows.
 * Thus, it's best to lazy load it.
 */
export const LazyJsonEditor = (props: JsonEditorProps) => {
  return <Suspense fallback={<LoadingSpinner/>}>
    <JsonEditor
      {...props}
    />
  </Suspense>
}
