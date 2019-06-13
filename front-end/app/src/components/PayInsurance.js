import React from 'react'
import {Link} from 'react-router-dom'
import { Form, Select, Input, InputNumber, Button } from 'antd';
import axios from 'axios'

const { Option } = Select;

class PayInsurance extends React.Component {
        constructor(props) {
                super(props);

                this.state = {
                    policies : []
                }
        }

        componentDidMount() {
            axios.get('http://localhost:10030/api/check-status')
                          .then(response => {
                                for(let state of response.data){
                                    this.setState(prevState => ({
                                                                                policies : [...prevState.policies, state]
                                                                        }))
                                }

                          })
                          .catch(error => {
                                console.log(error);
                          });
        }

      handleSubmit = e => {
        e.preventDefault();
        this.props.form.validateFields((err, values) => {
          if (!err) {
            axios.post("http://localhost:10030/api/pay-insurance", values)
                    .then(() => this.props.history.push('/check-status'))
          }
        });
      };

      handleSelectChange = value => {
          console.log(value);
          this.props.form.setFieldsValue({
            itemId: value,
          });
        };

      render() {
            const { getFieldDecorator } = this.props.form;

            return (
            <div className='one'>
                    <div className='container-home-screen'>
                         <h1 style={{marginLeft: '21%'}}>Pay insurance policy:</h1>
                         <div className='form'>
                             <Form labelCol={{ span: 5 }} wrapperCol={{ span: 12 }} onSubmit={this.handleSubmit}>
                                 <Form.Item label="Policy ID (you can check the policy Id you want to pay in the Check Status menu section)">
                                   {getFieldDecorator('itemId', {
                                     rules: [{ required: true, message: 'Please input your policy ID!' }],
                                   })(
//                                   <Select
//                                                    placeholder="Select a option and change input text above"
//                                                    onChange={this.handleSelectChange}
//                                                  >
//                                                  {this.state.policies.map((policy) =>
//                                                    <Option key={policy.linearID} value={policy.linearID}>{policy.Id}</Option>
//                                                  )}
//                                                  </Select>
                                                  <Input />
                                                  )}
                                 </Form.Item>
                                 <Form.Item label="Pay amount (GBP)" style={{marginTop: '5%'}}>
                                           {getFieldDecorator('amount', {
                                             rules: [{ required: true, message: 'Please input your amount!' }],
                                           })(<InputNumber min={0.1}/>)}
                                         </Form.Item>
                                 <Form.Item wrapperCol={{ span: 12, offset: 5 }}>
                                   <Button type="primary" htmlType="submit" style={{marginTop: '5%'}}>
                                     Submit
                                   </Button>
                                 </Form.Item>
                             </Form>

                             <div style={{marginTop: '30px'}}>Unfortunately AntDesign, which I'm using for a better UI, is not working well with Kotlin, so I can't use it's
                              Select component. I can think about a work round solution, but for this testing
                               app I'd prefer not to go so much into the front end details'</div>
                         </div>
                    </div>
            </div>
            );
      }
}

const WrappedPayInsurance = Form.create({ name: 'coordinated' })(PayInsurance);

export default WrappedPayInsurance