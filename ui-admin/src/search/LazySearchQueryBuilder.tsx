import React, {
  lazy,
  Suspense
} from 'react'
import { StudyEnvContextT } from '../study/StudyEnvironmentRouter'
import LoadingSpinner from '../util/LoadingSpinner'


const SearchQueryBuilder = lazy(() => import('./SearchQueryBuilder'))

/**
 * Antlr is a fairly large library, and it's only needed for specific workflows.
 * Thus, it's best to lazy load it.
 */
export const LazySearchQueryBuilder = ({
  studyEnvContext,
  onSearchExpressionChange,
  searchExpression
}: {
  studyEnvContext: StudyEnvContextT,
  onSearchExpressionChange: (searchExpression: string) => void,
  searchExpression: string
}) => {
  return <Suspense fallback={<LoadingSpinner/>}>
    <SearchQueryBuilder
      studyEnvContext={studyEnvContext}
      onSearchExpressionChange={onSearchExpressionChange}
      searchExpression={searchExpression}
    />
  </Suspense>
}
