package org.smart4j.chapter2.controller;

import org.smart4j.chapter2.model.Customer;
import org.smart4j.chapter2.service.CustomerService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.View;
import javax.xml.ws.Action;
import java.io.IOException;
import java.util.List;


/**
@Controller
public class CustomerController{
    @Inject
    private CustomerService customerService;

    @Action("get://customer")
    public View index(Param param) {
        List<Customer> customerList=customerService.getCustomerList();
        return new View("customer.jsp").addModel("customerList",customerList);
    }

}

*/

/**
 * 创建客户
 */


@WebServlet("/customer")
public class CustomerCreateServlet extends HttpServlet {

    private CustomerService customerService;
    /**
     * 进入 创建客户 界面
     */
    @Override
    public void init() throws ServletException{
        customerService=new CustomerService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException{
        // TODO: 2018/5/7
        List<Customer> customerList=customerService.getCustomerList();
        req.setAttribute("customerList",customerList);
        req.getRequestDispatcher("WEB-INF/view/customer.jsp").forward(req,resp);

    }
}
