import { PageInterface } from 'src/models/page-interface'

export interface RegistrationPageInterface extends PageInterface {
  fillIn(question: string, value: string): Promise<this>
  select(question: string, value: string): Promise<this>
  check(question: string, value: string): Promise<this>
  click(role: string, name: string): Promise<this>
}
