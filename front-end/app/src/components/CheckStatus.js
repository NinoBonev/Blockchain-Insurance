import React from 'react'
import {List, Row, Col, Skeleton, Avatar} from 'antd';
import {Link} from 'react-router-dom'
import axios from 'axios'

class CheckStatus extends React.Component {

    constructor(props) {
            super(props);

            this.state = {
                policies: []
            };
        }

        componentDidMount() {
            window.scrollTo(0,0);
            this.fetchStatus()
        }

        async fetchStatus(){
            axios.get('http://localhost:10030/api/check-status', {
                                                                 crossDomain: true
                                                             })
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


    render() {
        return (
                <div className='one'>
                   <div className='container-home-screen'>
                   <Row gutter={16} >
                        <Col span={20} offset={2} >
                        <div style={{marginLeft: '5%'}}>(AntDesign Pagination is not looking cool with Kotlin again)</div>
                        {this.state.policies.length > 0 ? <List

                            itemLayout="vertical"
                            size="large"
                            pagination={{
                                align: 'bottom',
                                hideOnSinglePage: true,
                                pageSize: 4,
                            }}
                            dataSource={this.state.policies}
                            renderItem={policy => (
                                <List.Item
                                    key={policy.id}
                                    style={{margin: 10}}
                                >
                                    <div>
                                        <div key={policy.id}>Policy name: {policy.id}</div>
                                        <div key={policy.linearID}>Policy ID: {policy.linearID}</div>
                                        <div key={policy.price}>Policy amount: {policy.price}</div>
                                        <div key={policy.amountPaid}>Already paid amount: {policy.amountPaid}</div>
                                        <br />
                                    </div>
                                </List.Item>
                            )}
                        /> :
                            <div align="center"><h1>You don't have any policies signed, yet'</h1>
                            </div>

                        }
                        </Col>
                   </Row>
                   </div>
                </div>
            )
    }
}

export default CheckStatus