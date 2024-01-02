import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import EnrolleeSearchFacets from '../facets/EnrolleeSearchFacets'
import { Facet, FacetValue } from '../../../../api/enrolleeSearch'

/**
 * Implements a modal dialog for specifying specific search criteria for the participant list.
 */
const AdvancedSearchModal = ({ onDismiss, updateFacetValues, facetValues, searchCriteria }:
                           { onDismiss: () => void,
                             updateFacetValues: (values: FacetValue[]) => void,
                             facetValues: FacetValue[],
                             searchCriteria: Facet[]
                           }) => {
  const [localFacets, setLocalFacets] = useState(facetValues)

  const updateLocalFacets = (facetValues: FacetValue[]) => {
    setLocalFacets(facetValues)
  }

  const searchOnClick = () => {
    updateFacetValues(localFacets)
    onDismiss()
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Particpant search</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <EnrolleeSearchFacets facets={searchCriteria} facetValues={localFacets}
          updateFacetValues={updateLocalFacets}/>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <button className="btn btn-primary" onClick={searchOnClick}>Search</button>
      <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
    </Modal.Footer>
  </Modal>
}

export default AdvancedSearchModal
