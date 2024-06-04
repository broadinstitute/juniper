import React, { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { portalHomePath } from './portal/PortalRouter'
import { useNavContext } from './navbar/NavContextProvider'
import Api, { getMediaUrl } from './api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCircle, faExternalLink, faList, faSearch, faTableCellsLarge } from '@fortawesome/free-solid-svg-icons'
import { Portal } from '@juniper/ui-core/build/types/portal'
import { basicTableLayout } from './util/tableUtils'
import { ColumnDef, getCoreRowModel, getSortedRowModel, SortingState, useReactTable } from '@tanstack/react-table'
import { useConfig } from './providers/ConfigProvider'
import { instantToDateString } from '@juniper/ui-core'

/** Shows a user the list of portals available to them */
function HomePage() {
  const { portalList } = useNavContext()
  const [portalSearch, setPortalSearch] = useState('')
  const [view, setView] = useState<'grid' | 'list'>('grid')
  const filteredPortalList = useMemo(() => portalList.filter(portal =>
    portal.name.toLowerCase().includes(portalSearch.toLowerCase()) ||
    portal.portalStudies.some(portalStudy => portalStudy.study.name.toLowerCase().includes(portalSearch.toLowerCase()))
  ), [portalList, portalSearch])

  return <div className="container" style={{ minHeight: '100vh' }}>
    <h1 className="h2 d-flex justify-content-center pb-2">Select a portal</h1>
    <div className="d-flex align-items-center pb-2">
      <form className="rounded-5 m-auto" onSubmit={e => {
        e.preventDefault()
      }} style={{
        border: '1px solid #bbb',
        backgroundColor: '#fff',
        padding: '0.25em 0.75em 0em'
      }}>
        <button type="submit" title="submit search" className="btn btn-secondary">
          <FontAwesomeIcon icon={faSearch}/>
        </button>
        <input type="text" value={portalSearch} size={50}
          title={'Search for portals or studies'}
          style={{ border: 'none', outline: 'none' }}
          placeholder={'Search for portals or studies'}
          onChange={e => setPortalSearch(e.target.value)}/>
      </form>
      <div className="btn-group position-absolute end-0 px-3">
        <button id="grid" className={`btn btn-sm ${view === 'grid' ? 'btn-dark' : 'btn-light'}`}
          onClick={() => setView('grid')}>
          <FontAwesomeIcon icon={faTableCellsLarge}/> Grid
        </button>
        <button id="list" className={`btn btn-sm ${view === 'list' ? 'btn-dark' : 'btn-light'}`}
          onClick={() => setView('list')}>
          <FontAwesomeIcon icon={faList}/> List
        </button>
      </div>
    </div>

    {portalList.length === 0 ?
      <div className="d-flex justify-content-center mt-3">
        <div className="alert alert-warning" role="alert">
            You do not have access to any portals or studies. If this is an error, please
            contact <a href="mailto:support@juniper.terra.bio">support@juniper.terra.bio</a>
        </div>
      </div> :
      <div className='d-flex justify-content-center py-4'>
        <div style={{ width: '90%' }}>
          {view === 'grid' ?
            <PortalGrid portalList={filteredPortalList}/> :
            <PortalList portalList={filteredPortalList}/>
          }
        </div>
      </div>
    }
  </div>
}

function PortalGrid({ portalList }: { portalList: Portal[] }) {
  return (
    <div className="d-flex flex-wrap justify-content-center">
      {portalList.map(portal => (
        <Link key={portal.shortcode} to={portalHomePath(portal.shortcode)}>
          <div className="card m-2 shadow-sm d-flex flex-column" style={{ width: '200px', height: '200px' }}>
            <div className="card-body p-0">
              <h6 className="card-title m-2 fw-bold">{portal.name}</h6>
            </div>
            <div className="d-flex flex-grow-1 m-auto"
              style={{ width: '100%', maxWidth: '150px', maxHeight: '150px' }}>
              <img src={getMediaUrl(portal.shortcode, 'favicon.ico', 'latest')}
                className="card-img-top"
                alt={portal.name} style={{ objectFit: 'contain', maxWidth: '100%', maxHeight: '80%' }}/>
            </div>
          </div>
        </Link>
      ))}
    </div>
  )
}

function PortalList({ portalList }: { portalList: Portal[] }) {
  const zoneConfig = useConfig()
  const [sorting, setSorting] = React.useState<SortingState>([])

  const columns: ColumnDef<Portal>[] = [{
    header: 'Portal Name',
    accessorKey: 'name',
    cell: ({ row }) => (
      <div className="d-flex">
        <div className='d-flex align-items-center justify-content-center' style={{ width: '1.5em', height: '1.5em' }}>
          <img src={getMediaUrl(row.original.shortcode, 'favicon.ico', 'latest')}
            style={{ maxHeight: '100%', maxWidth: '100%' }} className="me-3" alt={row.original.name}/>
        </div>
        <Link to={portalHomePath(row.original.shortcode)}>
          {row.original.name}
        </Link>
      </div>)
  }, {
    header: 'Status',
    cell: ({ row }) => {
      const portal = row.original
      const livePortalEnv = portal.portalEnvironments?.find(pe => pe.environmentName === 'live')
      return livePortalEnv && <>
        {livePortalEnv.portalEnvironmentConfig?.acceptingRegistration ?
          <><FontAwesomeIcon icon={faCircle} className={'fa-xs text-success'}/> Live</> :
          <><FontAwesomeIcon icon={faCircle} className={'fa-xs text-muted'}/> Closed</>}
      </>
    }
  }, {
    header: 'Website',
    cell: ({ row }) => {
      const portal = row.original
      const portalEnv = portal.portalEnvironments?.find(pe => pe.environmentName === 'live')
      return (
        portalEnv ?
          <a href={Api.getParticipantLink(portalEnv.portalEnvironmentConfig, zoneConfig.participantUiHostname,
            portal.shortcode, portalEnv.environmentName)} target="_blank">
                Participant website <FontAwesomeIcon icon={faExternalLink}/>
          </a> :
          <span className="text-muted">Website not initialized</span>)
    }
  }, {
    header: 'Total Studies',
    accessorFn: portal => portal.portalStudies.length
  }, {
    header: 'Created',
    accessorFn: portal => {
      //Sandbox was the very first environment created, so we'll run with that.
      //'Date Launched' could potentially be a better field to use, though
      const createdDate = portal.portalEnvironments?.find(pe =>
        pe.environmentName === 'sandbox')?.createdAt
      return instantToDateString(createdDate)
    }
  }]

  const table = useReactTable({
    data: portalList,
    columns,
    state: {
      sorting
    },
    onSortingChange: setSorting,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel()
  })

  return <div className="py-2 px-5 border rounded shadow-sm">
    {basicTableLayout(table, { tableClass: 'table' })}
  </div>
}


export default HomePage
