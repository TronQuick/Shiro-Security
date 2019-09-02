package shiro.dao;

import com.cheng.shiro.vo.User;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chengchenrui
 * @version Id: UserDao.java, v 0.1 2018/6/29 1:05 chengchenrui Exp $$
 */
@Component
public interface UserDao {
    
    User getUserByUsername(String username);

    List<String> getRolesByUserName(String username);
}