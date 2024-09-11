import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faUserLarge, faUserLargeSlash } from '@fortawesome/free-solid-svg-icons'
import { faUsers } from '@fortawesome/free-solid-svg-icons/faUsers'
import React from 'react'

type ParticipantView = 'participant' | 'family' | 'withdrawn'

type ParticipantListViewSwitcherProps = {
    view: ParticipantView
    setView: (view: ParticipantView) => void
    setSearchParams: (params: { view: string }) => void
    familyLinkageEnabled: boolean
}

export const ParticipantListViewSwitcher = (props: ParticipantListViewSwitcherProps) => {
  const { view, setView, setSearchParams, familyLinkageEnabled } = props
  return (
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
  )
}
