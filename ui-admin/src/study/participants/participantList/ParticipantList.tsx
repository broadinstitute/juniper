import React, { useState } from 'react'
import Api, { EnrolleeSearchExpressionResult } from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'
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
import { useSingleSearchParam } from 'util/searchParamsUtils'
import { Link } from 'react-router-dom'

/** Shows a list of (for now) enrollees */
function ParticipantList({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { portal, study, currentEnv } = studyEnvContext
  const [participantList, setParticipantList] = useState<EnrolleeSearchExpressionResult[]>([])
  const [groupByFamilyString, setGroupByFamily] = useSingleSearchParam('groupByFamily')
  const groupByFamily = groupByFamilyString === 'true'

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
    <div className="d-flex align-content-center align-items-center">
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
            onClick={() => setGroupByFamily(groupByFamily ? 'false' : 'true')}>
            {groupByFamily
              ? <><FontAwesomeIcon size={'sm'} className={'p-0 m-0'} icon={faPerson}/> Participant view</>
              : <><FontAwesomeIcon size={'sm'} className={'p-0 m-0'} icon={faPeopleGroup}/> Family view</>}
          </Button>
        </div>
      }
      <div><Link to={`withdrawn`}>Withdrawn</Link></div>
    </div>


    <LoadingSpinner isLoading={isLoading}>
      {groupByFamily
        ? <ParticipantListTableGroupedByFamily participantList={participantList} studyEnvContext={studyEnvContext}/>
        : <ParticipantListTable participantList={participantList} studyEnvContext={studyEnvContext} reload={reload}/>}
    </LoadingSpinner>
  </div>
}

export default ParticipantList
