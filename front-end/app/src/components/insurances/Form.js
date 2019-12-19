import { Form, Select, Input, Button } from 'antd';
import React from 'react'
import axios from 'axios'

const { Option } = Select;

class FormClass extends React.Component {

    constructor(props) {
            super(props);
    }

  handleSubmit = e => {
    e.preventDefault();
    this.props.form.validateFields((err, values) => {
      if (!err) {
        axios.post("http://localhost:10030/api/sign-insurance", values)
                .then(() => this.props.history.push('/check-status'))
      }
    });
  };

  render() {
        const { getFieldDecorator } = this.props.form;

        return (
            <div style={{marginLeft: '21%'}}>
                <Form labelCol={{ span: 5 }} wrapperCol={{ span: 12 }} onSubmit={this.handleSubmit}>
                    <Form.Item label="Item Name">
                      {getFieldDecorator('itemId', {
                        rules: [{ required: true, message: 'Please input your object!' }],
                      })(<Input />)}
                    </Form.Item>
                    <Form.Item label="Account name" style={{marginTop: '5%'}}>
                                          {getFieldDecorator('accountName', {
                                            rules: [{ required: true, message: 'Please input your object!' }],
                                          })(<Input />)}
                                        </Form.Item>
                    <Form.Item label="Agreed Price (GBP)" style={{marginTop: '5%'}}>
                              {getFieldDecorator('price', {
                                rules: [{ required: true, message: 'Please input your price!' }],
                              })(<Input />)}
                            </Form.Item>
                    <Form.Item wrapperCol={{ span: 12, offset: 5 }}>
                      <Button type="primary" htmlType="submit" style={{marginTop: '5%'}}>
                        Submit
                      </Button>
                    </Form.Item>
                </Form>
            </div>
        );
  }
}

const WrappedApp = Form.create({ name: 'coordinated' })(FormClass);

export default WrappedApp