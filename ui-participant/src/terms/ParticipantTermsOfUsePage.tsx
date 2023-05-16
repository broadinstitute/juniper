import React from 'react'

import { ParticipantTermsOfUse } from '@juniper/ui-core'

import Navbar from '../Navbar'

const ParticipantTermsOfUsePage = () => {
  return (
    <div className="container-fluid bg-white min-vh-100 d-flex flex-column p-0">
      <Navbar aria-label="Primary" />
      <main className="flex-grow-1 py-5">
        <div className="row mx-0 justify-content-center">
          <div className="col-12 col-sm-10 col-lg-6">
            <ParticipantTermsOfUse />
          </div>
        </div>
      </main>
    </div>
  )
}

export default ParticipantTermsOfUsePage
