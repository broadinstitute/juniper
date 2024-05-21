import { JuniperPageInterface } from 'src/models/juniper-page-interface'
import Question from 'src/page-components/question'

export interface RegistrationPageInterface extends JuniperPageInterface {
  getQuestion(qText: string): Question
}
