package net.rrm.ehour.persistence.backup.dao;

import net.rrm.ehour.domain.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author thies (Thies Edeling - thies@te-con.nl)
 *         Created on: Nov 13, 2010 - 2:35:50 AM
 */
public enum BackupEntityType
{
    USER_DEPARTMENT(UserDepartment.class, 0),
    USER_ROLE(UserRole.class, 1),
    USERS(User.class, "USERLIST", 2),
    CUSTOMER(Customer.class, 3),
    PROJECT(Project.class, 4),
    ACTIVITY(Activity.class, 5),
    TIMESHEET_ENTRY(TimesheetEntry.class, "TIMESHEET_ENTRIES", 6, new TimesheetEntryRowProcessor()),
    TIMESHEET_COMMENT(TimesheetComment.class, 7),
    AUDIT(Audit.class, 8),
    USER_TO_USERROLE(9);

    private String parentName;
    private Class<? extends DomainObject<?, ?>> domainObjectClass;
    private int order;
    private BackupRowProcessor processor;

    private BackupEntityType(int order)
    {
        this(null, order);
    }

    private BackupEntityType(Class<? extends DomainObject<?, ?>> domainObjectClass, int order)
    {
        this.domainObjectClass = domainObjectClass;
        this.parentName = name() + "S";
        this.order = order;
    }

    private BackupEntityType(Class<? extends DomainObject<?, ?>> domainObjectClass, String parentName, int order)
    {
        this.domainObjectClass = domainObjectClass;
        this.parentName = parentName;
        this.order = order;
    }

    private BackupEntityType(Class<? extends DomainObject<?, ?>> domainObjectClass, String parentName, int order, BackupRowProcessor processor)
    {
        this(domainObjectClass, parentName, order);
        this.processor = processor;
    }

    public BackupRowProcessor getProcessor()
    {
        return processor;
    }

    public String getParentName()
    {
        return parentName;
    }

    public Class<? extends DomainObject<?, ?>> getDomainObjectClass()
    {
        return domainObjectClass;
    }

    public int getOrder()
    {
        return order;
    }

    public static BackupEntityType forClass(Class clazz)
    {
        for (BackupEntityType type : values())
        {
            if (type.getDomainObjectClass() == clazz)
            {
                return type;
            }
        }

        return null;
    }

    public static List<BackupEntityType> orderedValues()
    {
        List<BackupEntityType> types = Arrays.asList(BackupEntityType.values());

        Collections.sort(types, new Comparator<BackupEntityType>()
        {
            @Override
            public int compare(BackupEntityType o1, BackupEntityType o2)
            {
                return o1.order - o2.order;
            }
        });

        return types;
    }

    public static List<BackupEntityType> reverseOrderedValues()
    {
        List<BackupEntityType> types = Arrays.asList(BackupEntityType.values());

        Collections.sort(types, new Comparator<BackupEntityType>()
        {
            @Override
            public int compare(BackupEntityType o1, BackupEntityType o2)
            {
                return o2.order - o1.order;
            }
        });

        return types;
    }
}
