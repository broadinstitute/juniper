/* eslint-disable jest/expect-expect */
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import React from 'react'

import { FormContent } from '@juniper/ui-core'

import { FormTableOfContents } from './FormTableOfContents'

const formContent: FormContent = {
  title: 'Test Survey',
  pages: [
    {
      elements: [
        {
          type: 'html',
          name: 'page1_intro',
          html: '<h1>Name</h1>'
        },
        {
          type: 'panel',
          elements: [
            {
              name: 'first_name',
              type: 'text',
              title: 'First name?'
            },
            {
              name: 'last_name',
              type: 'text',
              title: 'Last name?'
            }
          ]
        }
      ]
    },
    {
      elements: [
        {
          name: 'address',
          questionTemplateName: 'address_template'
        }
      ]
    }
  ],
  questionTemplates: [
    {
      name: 'address_template',
      type: 'text',
      title: 'Address?'
    }
  ]
}

describe('FormTableOfContents', () => {
  it('renders table of contents as a tree', () => {
    // Act
    render(
      <FormTableOfContents
        formContent={formContent}
        selectedElementName={undefined}
        onSelectElement={jest.fn()}
      />
    )

    // Assert
    ;['page1_intro', 'first_name', 'last_name', 'address', 'address_template'].forEach(questionName => {
      screen.getByText(questionName)
    })

    const getTreeItemChildren = (name: string) => {
      const treeItem = screen.getByText(name)
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      const childListItems = Array.from(treeItem.parentElement!.querySelector('ul')!.children)
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      return childListItems.map(el => el.querySelector('a')!.textContent)
    }

    expect(getTreeItemChildren('Form')).toEqual(['Pages', 'Question templates'])
    expect(getTreeItemChildren('Pages')).toEqual(['Page 1', 'Page 2'])
    expect(getTreeItemChildren('Page 1')).toEqual(['page1_intro', 'Panel (2 elements)'])
    expect(getTreeItemChildren('Page 2')).toEqual(['address'])
    expect(getTreeItemChildren('Question templates')).toEqual(['address_template'])
  })

  it.each(['page1_intro', 'first_name'])('allows selecting html elements and questions', async elementName => {
    // Arrange
    const user = userEvent.setup()

    const onSelectElement = jest.fn()
    render(
      <FormTableOfContents
        formContent={formContent}
        selectedElementName={undefined}
        onSelectElement={onSelectElement}
      />
    )

    // Act
    await user.click(screen.getByText(elementName))

    // Assert
    expect(onSelectElement).toHaveBeenCalledWith(elementName)
  })

  it.each(['Page 1', 'Panel (2 elements)'])('it does not allow selecting other elements', async elementName => {
    // Arrange
    const user = userEvent.setup()

    const onSelectElement = jest.fn()
    render(
      <FormTableOfContents
        formContent={formContent}
        selectedElementName={undefined}
        onSelectElement={onSelectElement}
      />
    )

    // Act
    await user.click(screen.getByText(elementName))

    // Assert
    expect(onSelectElement).not.toHaveBeenCalled()
  })
})
