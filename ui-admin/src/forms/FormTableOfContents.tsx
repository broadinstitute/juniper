import React from 'react'

import { FormContent, FormElement } from '@juniper/ui-core'

import { Tree, TreeItemT } from 'components/Tree'

type FormContentTableOfContentsTreeItem = TreeItemT<{
  /** If this item can be selected. */
  isSelectable: boolean

  /** Path to the form element associated with this tree item in object notation. For example, 'pages[0].elements[1]' */
  path: string
}>

/** Recursive helper for getTableOfContentsTree. */
const getTableOfContentsTreeHelper = (parentPath: string) => {
  return (formElement: FormElement, elementIndex: number): FormContentTableOfContentsTreeItem => {
    if ('type' in formElement && formElement.type === 'panel') {
      return {
        label: <span>Panel <span className="fw-light fst-italic">({formElement.elements.length} items)</span></span>,
        data: {
          isSelectable: true,
          path: `${parentPath}[${elementIndex}]`
        },
        children: formElement.elements.map(getTableOfContentsTreeHelper(`${parentPath}[${elementIndex}].elements`))
      }
    } else {
      return {
        label: formElement.name,
        data: {
          isSelectable: true,
          path: `${parentPath}[${elementIndex}]`
        }
      }
    }
  }
}

/** Convert a FormContent object into a TreeItemT to render the table of contents as a string. */
export const getTableOfContentsTree = (formContent: FormContent): FormContentTableOfContentsTreeItem => {
  return {
    data: {
      isSelectable: true,
      path: 'pages'
    },
    children: (formContent.pages || []).map((page, pageIndex) => ({
      label: `Page ${pageIndex + 1}`,
      data: {
        isSelectable: true,
        path: `pages[${pageIndex}]`
      },
      children: page.elements.map(getTableOfContentsTreeHelper(`pages[${pageIndex}].elements`))
    }))
  }
}

type FormTableOfContentsProps = {
  formContent: FormContent
  selectedElementPath: string | undefined
  onSelectElement: (path: string) => void
}

/** Render a table of contents for a form. */
export const FormTableOfContents = (props: FormTableOfContentsProps) => {
  const { formContent, selectedElementPath, onSelectElement } = props

  return (
    <Tree
      id="form-table-of-contents"
      isItemSelected={item => item.data.path === selectedElementPath}
      label="Table of contents"
      rootItem={getTableOfContentsTree(formContent)}
      onClickItem={item => {
        const { isSelectable, path } = item.data
        if (isSelectable) {
          onSelectElement(path)
        }
      }}
    />
  )
}
