import React, {
  lazy,
  Suspense
} from 'react'
import { JsonEditorProps } from 'util/json/JsonEditor'
import LoadingSpinner from 'util/LoadingSpinner'


const JsonEditor = lazy(() => import('./JsonEditor'))

/**
 * the code editor is a large library and is only needed for specific workflows, so
 * it's best to lazy load it.
 */
export const LazyJsonEditor = (props: JsonEditorProps) => {
  return <Suspense fallback={<LoadingSpinner/>}>
    <JsonEditor
      {...props}
    />
  </Suspense>
}
