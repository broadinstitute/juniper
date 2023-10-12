
import {mockStudyEnvContext} from "../test-utils/mocking-utils";
import {setupRouterTest} from "../test-utils/router-testing-utils";
import CreateSurveyModal from "./surveys/CreateSurveyModal";
import {render, screen} from "@testing-library/react";
import React from "react";
import CreateNewStudyModal from "./CreateNewStudyModal";
import userEvent from "@testing-library/user-event";

describe('CreateNewStudyModal', () => {
    test('enables Create button when survey name and stable ID are filled out', async () => {
        const user = userEvent.setup()
        render(<CreateNewStudyModal onDismiss={jest.fn()}/>)

        const nameInput = screen.getByLabelText('Study name')
        const stableIdInput = screen.getByLabelText('Study shortcode')
        expect(screen.getByText('Create')).toHaveAttribute("aria-disabled", "true")
        await user.type(nameInput, 'Test study')
        await user.type(stableIdInput, 'teststudy')

        expect(screen.getByText('Create')).toHaveAttribute("aria-disabled", "false")
    })
})