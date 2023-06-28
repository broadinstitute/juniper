import { FormContent } from '@juniper/ui-core'

import { getTableOfContentsTree } from './FormTableOfContents'

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

describe('getTableOfContentsTree', () => {
  it('returns a table of contents tree', () => {
    // Act
    const tableOfContentsTree = getTableOfContentsTree(formContent)

    // Assert
    expect(tableOfContentsTree).toEqual({
      label: 'Form',
      data: {
        isSelectable: false,
        path: ''
      },
      children: [
        {
          label: 'Pages',
          data: {
            isSelectable: false,
            path: 'pages'
          },
          children: [
            {
              label: 'Page 1',
              data: {
                isSelectable: true,
                path: 'pages[0]'
              },
              children: [
                {
                  label: 'page1_intro',
                  data: {
                    isSelectable: true,
                    path: 'pages[0].elements[0]'
                  }
                },
                {
                  label: 'Panel (2 elements)',
                  data: {
                    isSelectable: true,
                    path: 'pages[0].elements[1]'
                  },
                  children: [
                    {
                      label: 'first_name',
                      data: {
                        isSelectable: true,
                        path: 'pages[0].elements[1].elements[0]'
                      }
                    },
                    {
                      label: 'last_name',
                      data: {
                        isSelectable: true,
                        path: 'pages[0].elements[1].elements[1]'
                      }
                    }
                  ]
                }
              ]
            },
            {
              label: 'Page 2',
              data: {
                isSelectable: true,
                path: 'pages[1]'
              },
              children: [
                {
                  label: 'address',
                  data: {
                    isSelectable: true,
                    path: 'pages[1].elements[0]'
                  }
                }
              ]
            }
          ]
        },
        {
          label: 'Question templates',
          data: {
            isSelectable: true,
            path: 'questionTemplates'
          },
          children: [
            {
              label: 'address_template',
              data: {
                isSelectable: true,
                path: 'questionTemplates[0]'
              }
            }
          ]
        }
      ]
    })
  })
})
