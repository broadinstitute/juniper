import React, { useState } from 'react'
import Api, { EnrolleeSearchExpressionResult } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { paramsFromContext, StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { useLoadingEffect } from 'api/api-utils'
import { renderPageHeader } from 'util/pageUtils'
import ParticipantSearch from './search/ParticipantSearch'
import { useParticipantSearchState } from 'util/participantSearchUtils'
import ParticipantListTableGroupedByFamily from 'study/participants/participantList/ParticipantListTableGroupedByFamily'
import ParticipantListTable from 'study/participants/participantList/ParticipantListTable'
import { ParticipantListViewSwitcher } from './ParticipantListViewSwitcher'

/** Shows a list of (for now) enrollees */
function ParticipantList({ studyEnvContext, groupByFamily }:
  {studyEnvContext: StudyEnvContextT, groupByFamily?: boolean}) {
  const { portal, study, currentEnv } = studyEnvContext
  const [participantList, setParticipantList] = useState<EnrolleeSearchExpressionResult[]>([])

  const familyLinkageEnabled = studyEnvContext.currentEnv.studyEnvironmentConfig.enableFamilyLinkage

  const {
    searchState,
    updateSearchState,
    setSearchState,
    facets,
    searchExpression
  } = useParticipantSearchState(['user.username', 'portalUser.lastLogin'],
    familyLinkageEnabled, paramsFromContext(studyEnvContext))

  const { isLoading, reload } = useLoadingEffect(async () => {
    const results = await Api.executeSearchExpression(
      portal.shortcode,
      study.shortcode,
      currentEnv.environmentName,
      searchExpression)
    setParticipantList(results)
  }, [portal.shortcode, study.shortcode, currentEnv.environmentName, searchExpression])

  return <div className="ParticipantList container-fluid px-4 py-2">
    <div className="d-flex align-items-center justify-content-between ">
      {renderPageHeader(groupByFamily ? 'Families' : 'Participants')}
      <ParticipantListViewSwitcher
        studyEnvConfig={currentEnv.studyEnvironmentConfig}
      />
    </div>
    <div className="d-flex align-content-center align-items-center justify-content-between">
      <ParticipantSearch
        key={currentEnv.environmentName}
        studyEnvContext={studyEnvContext}
        searchState={searchState}
        updateSearchState={updateSearchState}
        setSearchState={setSearchState}
        disabled={false}
      />
    </div>
    <LoadingSpinner isLoading={isLoading}>
      {groupByFamily && <ParticipantListTableGroupedByFamily
        participantList={participantList} studyEnvContext={studyEnvContext}/> }
      {!groupByFamily && <ParticipantListTable participantList={participantList}
        studyEnvContext={studyEnvContext} reload={reload}
        facetedSearchState={searchState} facets={facets}
        setSearchState={setSearchState}/>}
    </LoadingSpinner>
  </div>
}
export default ParticipantList
