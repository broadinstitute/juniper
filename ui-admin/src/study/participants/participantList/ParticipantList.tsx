import React, { useState } from 'react'
import Api, { EnrolleeSearchExpressionResult } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { useLoadingEffect } from 'api/api-utils'
import { renderPageHeader } from 'util/pageUtils'
import ParticipantSearch from './search/ParticipantSearch'
import { useParticipantSearchState } from 'util/participantSearchUtils'
import { concatSearchExpressions } from 'util/searchExpressionUtils'
import ParticipantListTableGroupedByFamily from 'study/participants/participantList/ParticipantListTableGroupedByFamily'
import ParticipantListTable from 'study/participants/participantList/ParticipantListTable'
import WithdrawnEnrolleeList from './WithdrawnEnrolleeList'
import { Route, Routes, useSearchParams } from 'react-router-dom'
import { ParticipantListViewSwitcher } from './ParticipantListViewSwitcher'
import PortalUserList from './PortalUserList'

/** Shows a list of (for now) enrollees */
function ParticipantList({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { portal, study, currentEnv } = studyEnvContext
  const [participantList, setParticipantList] = useState<EnrolleeSearchExpressionResult[]>([])



  const familyLinkageEnabled = studyEnvContext.currentEnv.studyEnvironmentConfig.enableFamilyLinkage

  const {
    searchState,
    updateSearchState,
    setSearchState,
    searchExpression
  } = useParticipantSearchState()

  const generateFullSearchExpression = () => {
    const expressions: string[] = [searchExpression, 'include({user.lastLogin})']
    if (familyLinkageEnabled) {
      expressions.push('include({family.shortcode})')
    }
    return concatSearchExpressions(expressions)
  }

  const { isLoading, reload } = useLoadingEffect(async () => {
    const results = await Api.executeSearchExpression(
      portal.shortcode,
      study.shortcode,
      currentEnv.environmentName,
      generateFullSearchExpression())

    setParticipantList(results)
  }, [portal.shortcode, study.shortcode, currentEnv.environmentName, searchExpression])

  return <div className="ParticipantList container-fluid px-4 py-2">
    {renderPageHeader('Participant List')}
    <div className="d-flex align-content-center align-items-center justify-content-between">
      <ParticipantSearch
        key={currentEnv.environmentName}
        studyEnvContext={studyEnvContext}
        searchState={searchState}
        updateSearchState={updateSearchState}
        setSearchState={setSearchState}
        disabled={view === 'withdrawn'}
      />
      <ParticipantListViewSwitcher
        setSearchParams={setSearchParams}
        familyLinkageEnabled={familyLinkageEnabled}
      />
    </div>

    <LoadingSpinner isLoading={isLoading}>
      <Routes>
        <Route path="withdrawn" element={<WithdrawnEnrolleeList studyEnvContext={studyEnvContext}/>}/>
        <Route path="portalUsers" element={<PortalUserList
          portalShortcode={studyEnvContext.portal.shortcode} envName={studyEnvContext.currentEnv.environmentName}
        />}/>
        <Route path="families" element={<ParticipantListTableGroupedByFamily
          participantList={participantList} studyEnvContext={studyEnvContext}/>}/>
      </Routes>
    </LoadingSpinner>
  </div>
}

export default ParticipantList
