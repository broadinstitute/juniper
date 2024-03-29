import React from 'react'

/** Shows a page indicating the person is ineligible to register for the portal */
export default function Ineligible() {
  return <div className="container text-center mt-5">
    <p>
      You are not currently eligible to participate in any current studies.
    </p>
    <p>
      Eligibility criteria can change, and new studies are expected to be added in the future that you may qualify for.
    </p>
    <form>
      Enter your email address to be notified when new study opportunities are available.<br/>
      <input type="email" size={30} className="m-2" placeholder="name@email.com"/><br/>
      <button type="button" className="btn btn-primary" onClick={() => alert('not implemented yet')}>Submit</button>
    </form>
  </div>
}
