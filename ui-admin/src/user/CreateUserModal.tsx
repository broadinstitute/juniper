import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import Api, { NewUser, Portal } from 'api/api'
import { useUser } from './UserProvider'
import LoadingSpinner from 'util/LoadingSpinner'
import { failureNotification, successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import Select from 'react-select'

const CreateUserModal = ({ show, setShow, portals, userCreated }:
                           {show: boolean, setShow: React.Dispatch<React.SetStateAction<boolean>>,
                             portals: Portal[], userCreated: () => void
                           }) => {
  const [isLoading, setIsLoading] = useState(false)
  const [portalShortcode, setPortalShortcode] =
    useState<string | null>(portals.length > 0 ? portals[0].shortcode : null)
  const { user } = useUser()
  const [newUser, setNewUser] = useState<NewUser>({ username: '', superuser: false, portalShortcode })
  const portalOpts = portals.map(portal => ({ label: portal.name, value: portal.shortcode }))
  const selectedPortalOpt = portalOpts.find(portalOpt => portalOpt.value === portalShortcode)

  const createUser = async () => {
    setIsLoading(true)
    try {
      const createdUser = await Api.createUser(newUser)
      Store.addNotification(successNotification(`${createdUser.username} created`))
      userCreated()
      setShow(false)
    } catch (e) {
      Store.addNotification(failureNotification('Error creating user'))
    }
    setIsLoading(false)
  }
  // username must be email-like, and either be a superuser or associated with a Portal (we're not yet supporting
  // mutliselect for users spanning multiple portals)
  const isUserValid = /^\S+@\S+\.\S+$/.test(newUser.username) && (newUser.superuser || newUser.portalShortcode)

  return <Modal show={show} onHide={() => setShow(false)}>
    <Modal.Header closeButton>
      <Modal.Title>Add admin user</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <div className="py-2">
          <div className="mb-3">
            <label className="form-label">
              Username
              <input type="email" value={newUser.username} className="form-control"
                onChange={e => setNewUser({ ...newUser, username: e.target.value })}/>
            </label>
          </div>
          { user.superuser && <div className="mb-3">
            <span>Superuser</span><br/>
            <label className="me-3">
              <input type="radio" name="superuser" value="true" checked={newUser.superuser}
                onChange={() => setNewUser({ ...newUser, portalShortcode: null, superuser: true })}
                className="me-1"/> Yes
            </label>
            <label>
              <input type="radio" name="superuser" value="false" checked={!newUser.superuser} className="me-1"
                onChange={() => setNewUser({ ...newUser, portalShortcode: null, superuser: false })}/> No
            </label>
          </div> }
          { !newUser.superuser && <div>
            <label>
              Portal
              <Select options={portalOpts} value={selectedPortalOpt}
                onChange={opt => setPortalShortcode(opt?.value ?? null)}/>
            </label>
          </div> }
        </div>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className="btn btn-primary" onClick={createUser} disabled={!isUserValid}>Create</button>
        <button className="btn btn-secondary" onClick={() => setShow(false)}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

export default CreateUserModal
