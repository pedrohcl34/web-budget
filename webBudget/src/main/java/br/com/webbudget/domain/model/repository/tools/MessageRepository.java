/*
 * Copyright (C) 2016 Arthur Gregorio, AG.Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.com.webbudget.domain.model.repository.tools;

import br.com.webbudget.application.component.table.Page;
import br.com.webbudget.application.component.table.PageRequest;
import br.com.webbudget.domain.model.entity.tools.Message;
import br.com.webbudget.domain.model.repository.GenericRepository;
import br.com.webbudget.domain.model.security.User;
import javax.enterprise.context.Dependent;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Arthur Gregorio
 *
 * @version 1.0.0
 * @since 2.2.0, 24/02/2016
 */
@Dependent
public class MessageRepository extends GenericRepository<Message, Long> implements IMessageRepository {

    /**
     * 
     * @param sender
     * @param filter
     * @param pageRequest
     * @return 
     */
    @Override
    public Page<Message> listSent(User sender, String filter, PageRequest pageRequest) {

        final Criteria criteria = this.createCriteria();

        if (filter != null) {
            criteria.add(Restrictions.or(
                    Restrictions.ilike("title", "%" + filter + "%"),
                    Restrictions.ilike("content", "%" + filter + "%")
            ));
        }
        
        criteria.add(Restrictions.eq("deleted", false));
        criteria.add(Restrictions.eq("sender", sender));
        
        // projetamos para pegar o total de paginas possiveis
        criteria.setProjection(Projections.count("id"));

        final Long totalRows = (Long) criteria.uniqueResult();

        // limpamos a projection para que a criteria seja reusada
        criteria.setProjection(null);
        criteria.setResultTransformer(Criteria.ROOT_ENTITY);
        
        // paginamos
        criteria.setFirstResult(pageRequest.getFirstResult());
        criteria.setMaxResults(pageRequest.getPageSize());

        if (pageRequest.getSortDirection() == PageRequest.SortDirection.ASC) {
            criteria.addOrder(Order.asc(pageRequest.getSortField()));
        } else if (pageRequest.getSortDirection() == PageRequest.SortDirection.DESC) {
            criteria.addOrder(Order.desc(pageRequest.getSortField()));
        }

        // montamos o resultado paginado
        return new Page<>(criteria.list(), totalRows);
    }
    
    
}
