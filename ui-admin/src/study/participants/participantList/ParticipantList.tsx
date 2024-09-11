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
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faUserLarge, faUserLargeSlash } from '@fortawesome/free-solid-svg-icons'
import { faUsers } from '@fortawesome/free-solid-svg-icons/faUsers'
import WithdrawnEnrolleeList from './WithdrawnEnrolleeList'
import { useSearchParams } from 'react-router-dom'

/** Shows a list of (for now) enrollees */
function ParticipantList({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { portal, study, currentEnv } = studyEnvContext
  const [participantList, setParticipantList] = useState<EnrolleeSearchExpressionResult[]>([])

  const [searchParams, setSearchParams] = useSearchParams()
  const [view, setView] = useState<'participant' | 'family' | 'withdrawn'>(
      searchParams.get('view') as never || 'participant')

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
      <div className="btn-group border my-1">
        <Button variant='light'
          aria-label={'Switch to withdrawn view'}
          className={`btn btn-sm ${view === 'withdrawn' ? 'btn-dark' : 'btn-light'}`}
          tooltip={'Switch to withdrawn view'}
          onClick={() => {
            setSearchParams({ view: 'withdrawn' })
            setView('withdrawn')
          }}>
          <FontAwesomeIcon icon={faUserLargeSlash}/>
        </Button>
        <Button variant='light'
          aria-label={'Switch to participant view'}
          className={`btn btn-sm ${view === 'participant' ? 'btn-dark' : 'btn-light'}`}
          tooltip={'Switch to participant view'}
          onClick={() => {
            setSearchParams({ view: 'participant' })
            setView('participant')
          }}>
          <FontAwesomeIcon icon={faUserLarge}/>
        </Button>
        { familyLinkageEnabled && <Button variant='light'
          aria-label={'Switch to family view'}
          className={`btn btn-sm ${view === 'family' ? 'btn-dark' : 'btn-light'}`}
          tooltip={'Switch to family view'}
          onClick={() => {
            setSearchParams({ view: 'family' })
            setView('family')
          }}>
          <FontAwesomeIcon icon={faUsers}/>
        </Button> }
      </div>
    </div>


    <LoadingSpinner isLoading={isLoading}>
      {view === 'family' &&
        <ParticipantListTableGroupedByFamily participantList={participantList} studyEnvContext={studyEnvContext}/>
      }
      {view === 'participant' &&
        <ParticipantListTable participantList={participantList} studyEnvContext={studyEnvContext} reload={reload}/>
      }
      {view === 'withdrawn' &&
        <WithdrawnEnrolleeList studyEnvContext={studyEnvContext}/>
      }
    </LoadingSpinner>
  </div>
}

export default ParticipantList
