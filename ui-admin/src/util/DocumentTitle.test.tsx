import { render, screen } from '@testing-library/react'
import React, { useState } from 'react'

import DocumentTitle from './DocumentTitle'
import { userEvent } from '@testing-library/user-event'


const DocTitleTestComponent = () => {
  const [showTitledComponent, setShowTitledComponent] = useState(false)
  return <div>
        hello
    { showTitledComponent && <div>
            subcomponent
      <DocumentTitle title="foo"/>
    </div>
    }
    <button onClick={() => setShowTitledComponent(!showTitledComponent)}>show subcomponent</button>
  </div>
}

describe('DocumentTitle', () => {
  it('updates, then restores the document title', async () => {
    render(<DocTitleTestComponent/>)
    expect(document.title).toEqual('')
    await userEvent.click(screen.getByText('show subcomponent'))
    expect(document.title).toEqual('foo | Juniper')
    await userEvent.click(screen.getByText('show subcomponent'))
    expect(document.title).toEqual('')
  })
})
