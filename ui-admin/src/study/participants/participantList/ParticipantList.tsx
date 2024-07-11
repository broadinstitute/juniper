import React, { useState } from 'react'
import Api, { EnrolleeSearchExpressionResult } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
import { useSearchParams } from 'react-router-dom'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faPeopleGroup,
  faPerson
} from '@fortawesome/free-solid-svg-icons'
import { useLoadingEffect } from 'api/api-utils'
import { renderPageHeader } from 'util/pageUtils'
import ParticipantSearch from './search/ParticipantSearch'
import { useParticipantSearchState } from 'util/participantSearchUtils'
import { concatSearchExpressions } from 'util/searchExpressionUtils'
import ParticipantListTableGroupedByFamily from 'study/participants/participantList/ParticipantListTableGroupedByFamily'
import ParticipantListTable from 'study/participants/participantList/ParticipantListTable'
import { Button } from 'components/forms/Button'

/** Shows a list of (for now) enrollees */
function ParticipantList({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { portal, study, currentEnv } = studyEnvContext
  const [participantList, setParticipantList] = useState<EnrolleeSearchExpressionResult[]>([])

  const [searchParams, setSearchParams] = useSearchParams()

  const groupByFamily = searchParams.get('groupByFamily') === 'true'
  const setGroupByFamily = (groupByFamily: boolean) => {
    setSearchParams(params => {
      params.set('groupByFamily', groupByFamily.toString())
      return params
    })
  }

  const familyLinkageEnabled = studyEnvContext.currentEnv.studyEnvironmentConfig.enableFamilyLinkage

  const {
    searchState,
    updateSearchState,
    setSearchState,
    searchExpression
  } = useParticipantSearchState()

  const { isLoading } = useLoadingEffect(async () => {
    const fullSearchExpression = concatSearchExpressions(
      [searchExpression, 'include({user.lastLogin})']
        .concat(familyLinkageEnabled ? ['include({family.shortcode})'] : [])
    )

    const results = await Api.executeSearchExpression(
      portal.shortcode,
      study.shortcode,
      currentEnv.environmentName,
      fullSearchExpression)

    setParticipantList(results)
  }, [portal.shortcode, study.shortcode, currentEnv.environmentName, searchExpression])


  return <div className="ParticipantList container-fluid px-4 py-2">
    {renderPageHeader('Participant List')}
    <div className="d-flex align-content-center">
      <ParticipantSearch
        key={currentEnv.environmentName}
        studyEnvContext={studyEnvContext}
        searchState={searchState}
        updateSearchState={updateSearchState}
        setSearchState={setSearchState}
      />
      {
        familyLinkageEnabled && <div className="d-flex align-content-center p-2">
          <Button
            variant="light" className="border btn-sm"
            aria-label={groupByFamily ? 'Participant view' : 'Family view'}
            onClick={() => setGroupByFamily(!groupByFamily)}>
            {groupByFamily
              ? <><FontAwesomeIcon size={'sm'} className={'p-0 m-0'} icon={faPerson}/> Participant view</>
              : <><FontAwesomeIcon size={'sm'} className={'p-0 m-0'} icon={faPeopleGroup}/> Family view</>}
          </Button>
        </div>
      }

    </div>


    <LoadingSpinner isLoading={isLoading}>
      {groupByFamily
        ? <ParticipantListTableGroupedByFamily participantList={participantList} studyEnvContext={studyEnvContext}/>
        : <ParticipantListTable participantList={participantList} studyEnvContext={studyEnvContext}/>}
    </LoadingSpinner>
  </div>
}


export default ParticipantList
